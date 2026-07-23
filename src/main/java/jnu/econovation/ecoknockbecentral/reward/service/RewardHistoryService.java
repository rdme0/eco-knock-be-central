package jnu.econovation.ecoknockbecentral.reward.service;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import jnu.econovation.ecoknockbecentral.reward.dto.RewardRecipient;
import jnu.econovation.ecoknockbecentral.reward.dto.request.RewardHistoryRequest;
import jnu.econovation.ecoknockbecentral.reward.dto.response.RewardHistoryResponse;
import jnu.econovation.ecoknockbecentral.reward.model.entity.RewardDistribution;
import jnu.econovation.ecoknockbecentral.reward.model.entity.RewardHistory;
import jnu.econovation.ecoknockbecentral.reward.repository.RewardHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RewardHistoryService {

    private final RewardHistoryRepository rewardHistoryRepository;

    @Transactional
    public void savePlannedHistories(
            RewardDistribution distribution,
            List<RewardRecipient> recipients
    ) {
        Instant now = Instant.now();
        recipients.forEach(recipient -> recipient.rewardDetails().forEach(detail ->
                rewardHistoryRepository.upsert(
                        now,
                        now,
                        recipient.memberId(),
                        distribution.getId(),
                        detail.rewardType().name(),
                        detail.rewardAmount(),
                        detail.stayHours(),
                        recipient.walletAddress()
                )
        ));
    }

    @Transactional(readOnly = true)
    public Page<RewardHistoryResponse> getMyHistory(Long memberId, RewardHistoryRequest request) {
        return rewardHistoryRepository.findConfirmedByMemberId(
                        memberId,
                        PageRequest.of(request.page(), request.size())
                )
                .map(this::toResponse);
    }

    private RewardHistoryResponse toResponse(RewardHistory history) {
        return new RewardHistoryResponse(
                history.getRewardType(),
                history.getRewardAmount(),
                history.getStayHours(),
                history.getRewardDistribution().getSettlementDate()
        );
    }
}
