package jnu.econovation.ecoknockbecentral.wallet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import jnu.econovation.ecoknockbecentral.member.repository.MemberRepository;
import jnu.econovation.ecoknockbecentral.wallet.model.entity.MemberWallet;
import jnu.econovation.ecoknockbecentral.wallet.model.vo.WalletType;
import jnu.econovation.ecoknockbecentral.wallet.repository.MemberWalletRepository;
import jnu.econovation.ecoknockbecentral.wallet.security.WalletPrivateKeyCipher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.web3j.crypto.Credentials;

class MemberWalletServiceTest {

    private EntityManager entityManager;
    private MemberRepository memberRepository;
    private MemberWalletRepository memberWalletRepository;
    private WalletPrivateKeyCipher privateKeyCipher;
    private MemberWalletService service;

    @BeforeEach
    void setUp() {
        entityManager = mock(EntityManager.class);
        memberRepository = mock(MemberRepository.class);
        memberWalletRepository = mock(MemberWalletRepository.class);
        privateKeyCipher = new WalletPrivateKeyCipher(randomEncryptionKey());
        service = new MemberWalletService(
                entityManager,
                memberRepository,
                memberWalletRepository,
                privateKeyCipher
        );
    }

    @Test
    void createsEncryptedManagedWalletForMember() {
        Member member = mock(Member.class);
        when(entityManager.find(Member.class, 1L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(member);
        when(memberWalletRepository.findByMemberIdAndWalletType(1L, WalletType.MANAGED))
                .thenReturn(Optional.empty());
        when(memberWalletRepository.existsByMemberIdAndActiveRewardDestinationTrue(1L))
                .thenReturn(false);
        when(memberWalletRepository.save(any(MemberWallet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MemberWallet wallet = service.createManagedWalletIfAbsent(1L);
        String privateKey = privateKeyCipher.decrypt(wallet.getEncryptedPrivateKey());

        assertThat(wallet.getMember()).isSameAs(member);
        assertThat(wallet.getWalletType()).isEqualTo(WalletType.MANAGED);
        assertThat(wallet.isActiveRewardDestination()).isTrue();
        assertThat(wallet.getEncryptedPrivateKey()).isNotEqualTo(privateKey);
        assertThat(Credentials.create(privateKey).getAddress()).isEqualTo(wallet.getWalletAddress());

        InOrder order = inOrder(entityManager, memberWalletRepository);
        order.verify(entityManager).find(Member.class, 1L, LockModeType.PESSIMISTIC_WRITE);
        order.verify(memberWalletRepository).findByMemberIdAndWalletType(1L, WalletType.MANAGED);
    }

    @Test
    void returnsExistingManagedWalletOnRepeatedCreation() {
        Member member = mock(Member.class);
        AtomicReference<MemberWallet> savedWallet = new AtomicReference<>();
        when(entityManager.find(Member.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(member);
        when(memberWalletRepository.findByMemberIdAndWalletType(2L, WalletType.MANAGED))
                .thenAnswer(invocation -> Optional.ofNullable(savedWallet.get()));
        when(memberWalletRepository.existsByMemberIdAndActiveRewardDestinationTrue(2L))
                .thenReturn(false);
        when(memberWalletRepository.save(any(MemberWallet.class))).thenAnswer(invocation -> {
            MemberWallet wallet = invocation.getArgument(0);
            savedWallet.set(wallet);
            return wallet;
        });

        MemberWallet first = service.createManagedWalletIfAbsent(2L);
        MemberWallet second = service.createManagedWalletIfAbsent(2L);

        assertThat(second).isSameAs(first);
        verify(memberWalletRepository, times(1)).save(any(MemberWallet.class));
    }

    @Test
    void createsOnlyMissingWalletsForExistingMembers() {
        Member withWallet = mock(Member.class);
        Member withoutWallet = mock(Member.class);
        MemberWallet existingWallet = mock(MemberWallet.class);
        when(withWallet.getId()).thenReturn(3L);
        when(withoutWallet.getId()).thenReturn(4L);
        when(entityManager.find(Member.class, 3L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(withWallet);
        when(entityManager.find(Member.class, 4L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(withoutWallet);
        when(memberRepository.findAll()).thenReturn(List.of(withWallet, withoutWallet));
        when(memberWalletRepository.findByMemberIdAndWalletType(3L, WalletType.MANAGED))
                .thenReturn(Optional.of(existingWallet));
        when(memberWalletRepository.findByMemberIdAndWalletType(4L, WalletType.MANAGED))
                .thenReturn(Optional.empty());
        when(memberWalletRepository.existsByMemberIdAndActiveRewardDestinationTrue(4L))
                .thenReturn(true);
        when(memberWalletRepository.save(any(MemberWallet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        int createdCount = service.createManagedWalletsForExistingMembers();

        assertThat(createdCount).isEqualTo(1);
        verify(memberWalletRepository).save(any(MemberWallet.class));
    }

    @Test
    void excludesGuestsFromExistingMemberWalletInitialization() {
        Member guest = mock(Member.class);
        when(guest.getRole()).thenReturn(Role.GUEST);
        when(memberRepository.findAll()).thenReturn(List.of(guest));

        int createdCount = service.createManagedWalletsForExistingMembers();

        assertThat(createdCount).isZero();
        verifyNoInteractions(entityManager, memberWalletRepository);
    }

    private String randomEncryptionKey() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}
