package jnu.econovation.ecoknockbecentral.reward.dto;

import jnu.econovation.ecoknockbecentral.reward.model.vo.RewardType;
import java.util.Objects;

public record RewardDetailDTO(RewardType rewardType, long rewardAmount, Integer stayHours) {
    public RewardDetailDTO {
        Objects.requireNonNull(rewardType, "rewardType must not be null");
        if (rewardAmount <= 0L) {
            throw new IllegalArgumentException("rewardAmount must be positive");
        }
        if (rewardType == RewardType.ATTENDANCE && stayHours != null) {
            throw new IllegalArgumentException("ATTENDANCE reward must not have stayHours");
        }
        if (rewardType == RewardType.STAY_DURATION && (stayHours == null || stayHours <= 0)) {
            throw new IllegalArgumentException("STAY_DURATION reward must have positive stayHours");
        }
    }
}
