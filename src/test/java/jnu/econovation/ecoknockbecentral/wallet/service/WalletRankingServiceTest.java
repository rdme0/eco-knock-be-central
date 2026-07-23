package jnu.econovation.ecoknockbecentral.wallet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort;
import jnu.econovation.ecoknockbecentral.wallet.client.KrtBalanceClient;
import jnu.econovation.ecoknockbecentral.wallet.dto.WalletRankingDTO;
import jnu.econovation.ecoknockbecentral.wallet.dto.WalletRankingEntryDTO;
import jnu.econovation.ecoknockbecentral.wallet.dto.request.WalletRankingRequest;
import jnu.econovation.ecoknockbecentral.wallet.dto.response.WalletRankingResponse;
import jnu.econovation.ecoknockbecentral.wallet.exception.KrtBalanceQueryException;
import jnu.econovation.ecoknockbecentral.wallet.model.entity.MemberWallet;
import jnu.econovation.ecoknockbecentral.wallet.repository.MemberWalletRepository;
import jnu.econovation.ecoknockbecentral.wallet.repository.WalletRankingCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WalletRankingServiceTest {

    private static final BigInteger ONE_KRT = BigInteger.TEN.pow(18);

    private MemberWalletRepository memberWalletRepository;
    private KrtBalanceClient krtBalanceClient;
    private WalletRankingCacheRepository cacheRepository;
    private WalletRankingService service;

    @BeforeEach
    void setUp() {
        memberWalletRepository = mock(MemberWalletRepository.class);
        krtBalanceClient = mock(KrtBalanceClient.class);
        cacheRepository = mock(WalletRankingCacheRepository.class);
        service = new WalletRankingService(memberWalletRepository, krtBalanceClient, cacheRepository);
    }

    @Test
    void returnsCachedPrefixWithoutDatabaseOrRpc() {
        WalletRankingDTO snapshot = new WalletRankingDTO(
                "KRT",
                Instant.parse("2026-07-23T01:00:00Z"),
                List.of(
                        new WalletRankingEntryDTO(1, 1L, "first", 32, "20"),
                        new WalletRankingEntryDTO(2, 2L, "second", 33, "10"),
                        new WalletRankingEntryDTO(3, 3L, "third", 34, "5")
                )
        );
        when(cacheRepository.findCurrent()).thenReturn(Optional.of(snapshot));

        WalletRankingResponse response = service.getRankings(new WalletRankingRequest(2));

        assertThat(response.rankings()).extracting(entry -> entry.memberId())
                .containsExactly(1L, 2L);
        verifyNoInteractions(memberWalletRepository, krtBalanceClient);
        verify(cacheRepository).findCurrent();
    }

    @Test
    void computesCompleteSnapshotWithRawBalanceOrderingTieBreakZeroExclusionAndLimit() {
        MemberWallet tiedHigherId = wallet(3L, "third", 33, "0x3");
        MemberWallet highest = wallet(4L, "highest", 34, "0x4");
        MemberWallet tiedLowerId = wallet(2L, "second", 32, "0x2");
        MemberWallet zero = wallet(1L, "zero", 31, "0x1");
        when(cacheRepository.findCurrent()).thenReturn(Optional.empty());
        when(memberWalletRepository.findActiveRewardDestinationsExcludingRole(any()))
                .thenReturn(List.of(tiedHigherId, highest, tiedLowerId, zero));
        when(krtBalanceClient.getBalance("0x3")).thenReturn(ONE_KRT.multiply(BigInteger.TEN));
        when(krtBalanceClient.getBalance("0x4")).thenReturn(ONE_KRT.multiply(BigInteger.valueOf(20)));
        when(krtBalanceClient.getBalance("0x2")).thenReturn(ONE_KRT.multiply(BigInteger.TEN));
        when(krtBalanceClient.getBalance("0x1")).thenReturn(BigInteger.ZERO);

        WalletRankingResponse response = service.getRankings(new WalletRankingRequest(2));

        assertThat(response.rankings())
                .extracting(entry -> entry.rank(), entry -> entry.memberId(), entry -> entry.balance())
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1, 4L, "20"),
                        org.assertj.core.groups.Tuple.tuple(2, 2L, "10")
                );
        ArgumentCaptor<WalletRankingDTO> snapshotCaptor = ArgumentCaptor.forClass(WalletRankingDTO.class);
        verify(cacheRepository).save(snapshotCaptor.capture());
        assertThat(snapshotCaptor.getValue().rankings())
                .extracting(WalletRankingEntryDTO::rank, WalletRankingEntryDTO::memberId)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1, 4L),
                        org.assertj.core.groups.Tuple.tuple(2, 2L),
                        org.assertj.core.groups.Tuple.tuple(3, 3L)
                );
    }

    @Test
    void doesNotOverwriteCachedSnapshotWhenAnyRpcFails() {
        MemberWallet first = wallet(1L, "first", 32, "0x1");
        MemberWallet second = wallet(2L, "second", 33, "0x2");
        when(cacheRepository.findCurrent()).thenReturn(Optional.empty());
        when(memberWalletRepository.findActiveRewardDestinationsExcludingRole(any()))
                .thenReturn(List.of(first, second));
        when(krtBalanceClient.getBalance("0x1")).thenReturn(ONE_KRT);
        when(krtBalanceClient.getBalance("0x2"))
                .thenThrow(new KrtBalanceQueryException(new IllegalStateException("rpc failure")));

        assertThatThrownBy(() -> service.getRankings(new WalletRankingRequest(3)))
                .isInstanceOf(KrtBalanceQueryException.class);
        verify(cacheRepository, never()).save(any());
    }

    private MemberWallet wallet(Long memberId, String name, int cohort, String address) {
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(memberId);
        when(member.getName()).thenReturn(name);
        when(member.getCohort()).thenReturn(new Cohort(cohort));
        MemberWallet wallet = mock(MemberWallet.class);
        when(wallet.getMember()).thenReturn(member);
        when(wallet.getWalletAddress()).thenReturn(address);
        return wallet;
    }
}
