package jnu.econovation.ecoknockbecentral.reward.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blockchain.ethereum-sepolia.reward-transaction")
public record RewardTransactionConfig(
        String distributorAddress
) {

    public RewardTransactionConfig {
        if (distributorAddress == null || distributorAddress.isBlank()) {
            throw new IllegalArgumentException("RewardDistributor address must not be blank");
        }
    }
}
