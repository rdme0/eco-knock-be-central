package jnu.econovation.ecoknockbecentral.reward.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import jnu.econovation.ecoknockbecentral.member.repository.MemberRepository;
import jnu.econovation.ecoknockbecentral.reward.dto.RewardSettlementResult;
import jnu.econovation.ecoknockbecentral.reward.model.vo.RewardType;
import jnu.econovation.ecoknockbecentral.wallet.model.entity.MemberWallet;
import jnu.econovation.ecoknockbecentral.wallet.repository.MemberWalletRepository;
import jnu.econovation.ecoknockbecentral.whozin.dto.WhozinMembersInternalDTO;
import jnu.econovation.ecoknockbecentral.whozin.dto.WhozinUser;
import jnu.econovation.ecoknockbecentral.whozin.service.WhozinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RewardSettlementServiceTest {

    private static final LocalDate SETTLEMENT_DATE = LocalDate.of(2026, 7, 21);
    private static final Cohort COHORT = new Cohort(32);
    private static final String MEMBER_NAME = "member-a";
    private static final String WALLET_ADDRESS = "0x6813d7a3e620f0c536876b41bfbb56779ea6e7e8";

    private WhozinService whozinService;
    private MemberRepository memberRepository;
    private MemberWalletRepository memberWalletRepository;
    private RewardSettlementService service;

    @BeforeEach
    void setUp() {
        whozinService = mock(WhozinService.class);
        memberRepository = mock(MemberRepository.class);
        memberWalletRepository = mock(MemberWalletRepository.class);
        service = new RewardSettlementService(whozinService, memberRepository, memberWalletRepository);
    }

    @Test
    void settlesMultipleMembersAndSortsRecipientsByWalletAddress() {
        Cohort otherCohort = new Cohort(33);
        Member firstMember = member(1L, COHORT, MEMBER_NAME, Role.USER);
        Member secondMember = member(2L, otherCohort, "member-b", Role.ADMIN);
        MemberWallet firstWallet = wallet("0xbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
        MemberWallet secondWallet = wallet("0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        WhozinUser firstUser = whozinUser(UUID.randomUUID(), COHORT, MEMBER_NAME, 30L);
        WhozinUser secondUser = whozinUser(UUID.randomUUID(), otherCohort, "member-b", 120L);
        stubWhozinUsers(firstUser, secondUser);
        when(memberRepository.findAll()).thenReturn(List.of(firstMember, secondMember));
        when(memberWalletRepository.findByMemberIdAndActiveRewardDestinationTrue(1L))
                .thenReturn(Optional.of(firstWallet));
        when(memberWalletRepository.findByMemberIdAndActiveRewardDestinationTrue(2L))
                .thenReturn(Optional.of(secondWallet));

        RewardSettlementResult result = service.settle(SETTLEMENT_DATE);

        assertThat(result.settlementDate()).isEqualTo(SETTLEMENT_DATE);
        assertThat(result.totalRewardAmount()).isEqualTo(12L);
        assertThat(result.recipients())
                .extracting(recipient -> recipient.walletAddress(), recipient -> recipient.rewardAmount())
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(secondWallet.getWalletAddress(), 7L),
                        org.assertj.core.groups.Tuple.tuple(firstWallet.getWalletAddress(), 5L)
                );
    }

    @ParameterizedTest
    @CsvSource({
            "29, 0",
            "30, 5",
            "59, 5",
            "60, 6",
            "119, 6",
            "120, 7"
    })
    void appliesRewardBoundaries(long presenceMinutes, long expectedReward) {
        Member member = member(1L, COHORT, MEMBER_NAME, Role.USER);
        MemberWallet wallet = wallet(WALLET_ADDRESS);
        stubWhozinUsers(whozinUser(UUID.randomUUID(), COHORT, MEMBER_NAME, presenceMinutes));
        when(memberRepository.findAll()).thenReturn(List.of(member));
        when(memberWalletRepository.findByMemberIdAndActiveRewardDestinationTrue(1L))
                .thenReturn(Optional.of(wallet));

        RewardSettlementResult result = service.settle(SETTLEMENT_DATE);

        assertThat(result.totalRewardAmount()).isEqualTo(expectedReward);
        if (expectedReward == 0L) {
            assertThat(result.recipients()).isEmpty();
            verifyNoInteractions(memberWalletRepository);
        } else {
            assertThat(result.recipients()).singleElement()
                    .satisfies(recipient -> {
                        assertThat(recipient.walletAddress()).isEqualTo(WALLET_ADDRESS);
                        assertThat(recipient.rewardAmount()).isEqualTo(expectedReward);
                        assertThat(recipient.rewardDetails().getFirst().rewardType()).isEqualTo(RewardType.ATTENDANCE);
                        if (presenceMinutes >= 60L) {
                            assertThat(recipient.rewardDetails()).hasSize(2);
                            assertThat(recipient.rewardDetails().get(1).rewardType()).isEqualTo(RewardType.STAY_DURATION);
                            assertThat(recipient.rewardDetails().get(1).stayHours()).isEqualTo((int) (presenceMinutes / 60L));
                        }
                    });
        }
    }

    @Test
    void excludesUnmatchedAndAmbiguousWhozinMembers() {
        Member firstDuplicate = member(1L, COHORT, MEMBER_NAME, Role.USER);
        Member secondDuplicate = member(2L, COHORT, MEMBER_NAME, Role.USER);
        WhozinUser unmatched = whozinUser(UUID.randomUUID(), new Cohort(31), "missing", 60L);
        WhozinUser ambiguous = whozinUser(UUID.randomUUID(), COHORT, MEMBER_NAME, 60L);
        stubWhozinUsers(unmatched, ambiguous);
        when(memberRepository.findAll()).thenReturn(List.of(firstDuplicate, secondDuplicate));

        RewardSettlementResult result = service.settle(SETTLEMENT_DATE);

        assertThat(result.recipients()).isEmpty();
        assertThat(result.totalRewardAmount()).isZero();
        verifyNoInteractions(memberWalletRepository);
    }

    @Test
    void excludesGuestFromMemberMatching() {
        Member guest = member(1L, COHORT, MEMBER_NAME, Role.GUEST);
        stubWhozinUsers(whozinUser(UUID.randomUUID(), COHORT, MEMBER_NAME, 60L));
        when(memberRepository.findAll()).thenReturn(List.of(guest));

        RewardSettlementResult result = service.settle(SETTLEMENT_DATE);

        assertThat(result.recipients()).isEmpty();
        verifyNoInteractions(memberWalletRepository);
    }

    @Test
    void excludesMemberWithoutActiveRewardWallet() {
        Member member = member(1L, COHORT, MEMBER_NAME, Role.USER);
        stubWhozinUsers(whozinUser(UUID.randomUUID(), COHORT, MEMBER_NAME, 60L));
        when(memberRepository.findAll()).thenReturn(List.of(member));
        when(memberWalletRepository.findByMemberIdAndActiveRewardDestinationTrue(1L))
                .thenReturn(Optional.empty());

        RewardSettlementResult result = service.settle(SETTLEMENT_DATE);

        assertThat(result.recipients()).isEmpty();
        assertThat(result.totalRewardAmount()).isZero();
    }

    @Test
    void rewardsDuplicatedWhozinUuidOnlyOnce() {
        Member member = member(1L, COHORT, MEMBER_NAME, Role.USER);
        MemberWallet wallet = wallet(WALLET_ADDRESS);
        UUID duplicatedUserId = UUID.randomUUID();
        stubWhozinUsers(
                whozinUser(duplicatedUserId, COHORT, MEMBER_NAME, 60L),
                whozinUser(duplicatedUserId, COHORT, MEMBER_NAME, 60L)
        );
        when(memberRepository.findAll()).thenReturn(List.of(member));
        when(memberWalletRepository.findByMemberIdAndActiveRewardDestinationTrue(1L))
                .thenReturn(Optional.of(wallet));

        RewardSettlementResult result = service.settle(SETTLEMENT_DATE);

        assertThat(result.recipients()).hasSize(1);
        assertThat(result.totalRewardAmount()).isEqualTo(6L);
        verify(memberWalletRepository, times(1))
                .findByMemberIdAndActiveRewardDestinationTrue(1L);
    }

    @Test
    void ignoresWhozinDataFromAnotherDate() {
        Member member = member(1L, COHORT, MEMBER_NAME, Role.USER);
        WhozinUser user = whozinUser(UUID.randomUUID(), COHORT, MEMBER_NAME, 60L);
        when(whozinService.getWhozinMembers(2026, 7, 21)).thenReturn(List.of(
                new WhozinMembersInternalDTO(SETTLEMENT_DATE.minusDays(1), List.of(user))
        ));
        when(memberRepository.findAll()).thenReturn(List.of(member));

        RewardSettlementResult result = service.settle(SETTLEMENT_DATE);

        assertThat(result.recipients()).isEmpty();
        verify(memberWalletRepository, never())
                .findByMemberIdAndActiveRewardDestinationTrue(1L);
    }

    private void stubWhozinUsers(WhozinUser... users) {
        when(whozinService.getWhozinMembers(2026, 7, 21)).thenReturn(List.of(
                new WhozinMembersInternalDTO(SETTLEMENT_DATE, List.of(users))
        ));
    }

    private Member member(Long id, Cohort cohort, String name, Role role) {
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(id);
        when(member.getCohort()).thenReturn(cohort);
        when(member.getName()).thenReturn(name);
        when(member.getRole()).thenReturn(role);
        return member;
    }

    private MemberWallet wallet(String address) {
        MemberWallet wallet = mock(MemberWallet.class);
        when(wallet.getWalletAddress()).thenReturn(address);
        return wallet;
    }

    private WhozinUser whozinUser(UUID id, Cohort cohort, String name, long presenceMinutes) {
        return new WhozinUser(id, cohort, name, Duration.ofMinutes(presenceMinutes));
    }
}
