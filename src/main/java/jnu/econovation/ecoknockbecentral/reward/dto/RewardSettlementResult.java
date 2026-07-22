package jnu.econovation.ecoknockbecentral.reward.dto;

import java.time.LocalDate;
import java.util.List;

public record RewardSettlementResult(
        LocalDate settlementDate,
        List<RewardRecipient> recipients,
        long totalRewardAmount
) {
    public RewardSettlementResult {
        recipients = List.copyOf(recipients);
    }
}
