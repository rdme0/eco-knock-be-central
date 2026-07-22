package jnu.econovation.ecoknockbecentral.reward.dto;

import java.math.BigInteger;

public record RewardTransactionDTO(
        String batchId,
        BigInteger rewardDay,
        String transactionHash
) {
}
