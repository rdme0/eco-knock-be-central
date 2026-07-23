package jnu.econovation.ecoknockbecentral.reward.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;

@Configuration
public class RewardWeb3jConfig {

    private static final long ETHEREUM_SEPOLIA_CHAIN_ID = 11_155_111L;
    private static final long RECEIPT_POLLING_INTERVAL_MILLIS = 1_000L;
    private static final int RECEIPT_POLLING_ATTEMPTS = 120;

    // Creates credentials for the operator wallet that signs reward distribution transactions
    @Bean
    public Credentials rewardOperatorCredentials(
            @Value("${blockchain.ethereum-sepolia.reward-transaction.operator-private-key}")
            String operatorPrivateKey
    ) {
        return Credentials.create(operatorPrivateKey);
    }

    // Polls Ethereum Sepolia until the submitted transaction receipt is available or times out(120s)
    @Bean
    public TransactionReceiptProcessor rewardTransactionReceiptProcessor(Web3j web3j) {
        return new PollingTransactionReceiptProcessor(
                web3j,
                RECEIPT_POLLING_INTERVAL_MILLIS,
                RECEIPT_POLLING_ATTEMPTS
        );
    }

    // Signs and submits transactions to Ethereum Sepolia using the configured operator wallet
    @Bean
    public TransactionManager rewardTransactionManager(
            Web3j web3j,
            Credentials rewardOperatorCredentials,
            TransactionReceiptProcessor rewardTransactionReceiptProcessor
    ) {
        return new RawTransactionManager(
                web3j,
                rewardOperatorCredentials,
                ETHEREUM_SEPOLIA_CHAIN_ID,
                rewardTransactionReceiptProcessor
        );
    }
}
