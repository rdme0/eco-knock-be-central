package jnu.econovation.ecoknockbecentral.reward.dto;

import java.math.BigInteger;

public record RewardTransactionResult(
        String batchId,
        BigInteger rewardDay,
        String transactionHash
) {
}
