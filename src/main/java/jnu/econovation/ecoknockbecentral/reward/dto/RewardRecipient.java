package jnu.econovation.ecoknockbecentral.reward.dto;

import java.util.List;
import java.util.Objects;

public record RewardRecipient(Long memberId, String walletAddress, List<RewardDetailDTO> rewardDetails) {
    public RewardRecipient {
        Objects.requireNonNull(memberId, "memberId must not be null");
        Objects.requireNonNull(walletAddress, "walletAddress must not be null");
        rewardDetails = List.copyOf(rewardDetails);
    }

    public long rewardAmount() {
        return rewardDetails.stream().mapToLong(RewardDetailDTO::rewardAmount).reduce(0L, Math::addExact);
    }
}
