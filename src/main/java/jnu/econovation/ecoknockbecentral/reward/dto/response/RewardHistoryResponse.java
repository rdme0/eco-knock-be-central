package jnu.econovation.ecoknockbecentral.reward.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import jnu.econovation.ecoknockbecentral.reward.model.vo.RewardType;

public record RewardHistoryResponse(
        @Schema(description = "보상 유형", example = "ATTENDANCE")
        RewardType rewardType,
        @Schema(description = "보상 금액", example = "5")
        long rewardAmount,
        @Schema(description = "체류 시간(시간 단위)", example = "2", nullable = true)
        Integer stayHours,
        @Schema(description = "정산 기준일(YYYY-MM-DD)", example = "2026-07-21")
        LocalDate settlementDate
) {
}
