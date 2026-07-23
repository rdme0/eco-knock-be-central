package jnu.econovation.ecoknockbecentral.reward.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import jnu.econovation.ecoknockbecentral.member.repository.MemberRepository;
import jnu.econovation.ecoknockbecentral.reward.dto.RewardDetailDTO;
import jnu.econovation.ecoknockbecentral.reward.dto.RewardRecipient;
import jnu.econovation.ecoknockbecentral.reward.dto.RewardSettlementResult;
import jnu.econovation.ecoknockbecentral.reward.model.vo.RewardType;
import jnu.econovation.ecoknockbecentral.wallet.model.entity.MemberWallet;
import jnu.econovation.ecoknockbecentral.wallet.repository.MemberWalletRepository;
import jnu.econovation.ecoknockbecentral.whozin.dto.WhozinUser;
import jnu.econovation.ecoknockbecentral.whozin.service.WhozinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardSettlementService {

    private static final long ATTENDANCE_REWARD = 5L;
    private static final long HOURLY_REWARD = 1L;
    private static final long MINIMUM_ATTENDANCE_MINUTES = 30L;
    private static final long MINUTES_PER_HOUR = 60L;

    private final WhozinService whozinService;
    private final MemberRepository memberRepository;
    private final MemberWalletRepository memberWalletRepository;

    public RewardSettlementResult settle(LocalDate settlementDate) {
        List<WhozinUser> whozinUsers = whozinService.getWhozinMembers(
                        settlementDate.getYear(),
                        settlementDate.getMonthValue(),
                        settlementDate.getDayOfMonth()
                ).stream()
                .filter(dailyMembers -> dailyMembers.getDate().equals(settlementDate))
                .flatMap(dailyMembers -> dailyMembers.getUsers().stream())
                .toList();

        List<Member> members = memberRepository.findAll().stream()
                .filter(member -> member.getRole() != Role.GUEST)
                .toList();

        Set<UUID> processedWhozinUserIds = new HashSet<>();
        Set<Long> rewardedMemberIds = new HashSet<>();
        List<RewardRecipient> recipients = new ArrayList<>();
        long totalRewardAmount = 0L;

        for (WhozinUser whozinUser : whozinUsers) {
            if (!processedWhozinUserIds.add(whozinUser.getUserId())) {
                log.warn(
                        "WhozIn 중복 회원으로 보상 정산 제외: settlementDate={}, cohort={}",
                        settlementDate,
                        whozinUser.getCohort().value()
                );
                continue;
            }

            List<RewardDetailDTO> rewardDetails = calculateRewardDetails(whozinUser.getPresenceDuration());
            if (rewardDetails.isEmpty()) {
                continue;
            }

            List<Member> matchedMembers = matchMembers(members, whozinUser);
            if (matchedMembers.size() != 1) {
                log.warn(
                        "WhozIn 회원 매칭 실패로 보상 정산 제외: settlementDate={}, cohort={}, matchCount={}",
                        settlementDate,
                        whozinUser.getCohort().value(),
                        matchedMembers.size()
                );
                continue;
            }

            Member member = matchedMembers.getFirst();
            if (!rewardedMemberIds.add(member.getId())) {
                log.warn(
                        "중앙 회원 중복 매칭으로 보상 정산 제외: settlementDate={}, cohort={}",
                        settlementDate,
                        whozinUser.getCohort().value()
                );
                continue;
            }

            MemberWallet wallet = memberWalletRepository
                    .findByMemberIdAndActiveRewardDestinationTrue(member.getId())
                    .orElse(null);
            if (wallet == null) {
                log.warn(
                        "활성 보상 지갑이 없어 보상 정산 제외: settlementDate={}, cohort={}",
                        settlementDate,
                        whozinUser.getCohort().value()
                );
                continue;
            }

            RewardRecipient recipient = new RewardRecipient(member.getId(), wallet.getWalletAddress(), rewardDetails);
            recipients.add(recipient);
            long rewardAmount = recipient.rewardAmount();
            totalRewardAmount = Math.addExact(totalRewardAmount, rewardAmount);
        }

        recipients.sort(Comparator.comparing(RewardRecipient::walletAddress));
        return new RewardSettlementResult(settlementDate, recipients, totalRewardAmount);
    }

    private List<Member> matchMembers(List<Member> members, WhozinUser whozinUser) {
        return members.stream()
                .filter(member -> Objects.equals(member.getCohort(), whozinUser.getCohort()))
                .filter(member -> Objects.equals(member.getName(), whozinUser.getName()))
                .toList();
    }

    private List<RewardDetailDTO> calculateRewardDetails(Duration presenceDuration) {
        long presenceMinutes = presenceDuration.toMinutes();
        if (presenceMinutes < MINIMUM_ATTENDANCE_MINUTES) {
            return List.of();
        }

        long fullHours = presenceMinutes / MINUTES_PER_HOUR;
        List<RewardDetailDTO> rewardDetails = new ArrayList<>();
        rewardDetails.add(new RewardDetailDTO(RewardType.ATTENDANCE, ATTENDANCE_REWARD, null));
        if (fullHours > 0L) {
            rewardDetails.add(new RewardDetailDTO(
                    RewardType.STAY_DURATION,
                    Math.multiplyExact(fullHours, HOURLY_REWARD),
                    Math.toIntExact(fullHours)
            ));
        }
        return rewardDetails;
    }
}
