package jnu.econovation.ecoknockbecentral.wallet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blockchain.ethereum-sepolia")
public record EthereumSepoliaConfig(
        String rpcUrl,
        String krtTokenAddress
) {

    public EthereumSepoliaConfig {
        if (rpcUrl == null || rpcUrl.isBlank()) {
            throw new IllegalArgumentException("Ethereum Sepolia RPC URL must not be blank");
        }
        if (krtTokenAddress == null || krtTokenAddress.isBlank()) {
            throw new IllegalArgumentException("KRT token address must not be blank");
        }
    }
}
