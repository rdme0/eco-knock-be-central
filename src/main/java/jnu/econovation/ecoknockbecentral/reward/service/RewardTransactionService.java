package jnu.econovation.ecoknockbecentral.reward.service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import jnu.econovation.ecoknockbecentral.reward.client.RewardDistributorClient;
import jnu.econovation.ecoknockbecentral.reward.dto.RewardRecipient;
import jnu.econovation.ecoknockbecentral.reward.dto.RewardSettlementResult;
import jnu.econovation.ecoknockbecentral.reward.dto.RewardTransactionResult;
import jnu.econovation.ecoknockbecentral.reward.exception.RewardTransactionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Hash;

@Service
@RequiredArgsConstructor
public class RewardTransactionService {

    private static final String BATCH_ID_PREFIX = "eco-knock:daily-reward:";
    private static final BigInteger TOKEN_UNIT = BigInteger.TEN.pow(18);

    private final RewardDistributorClient rewardDistributorClient;

    public Optional<RewardTransactionResult> submit(RewardSettlementResult settlement) {
        validateSettlement(settlement);
        if (settlement.recipients().isEmpty()) {
            return Optional.empty();
        }

        String batchId = createBatchId(settlement.settlementDate());
        BigInteger rewardDay = new BigInteger(
                settlement.settlementDate().format(DateTimeFormatter.BASIC_ISO_DATE)
        );
        List<String> recipients = settlement.recipients().stream()
                .map(RewardRecipient::walletAddress)
                .toList();
        List<BigInteger> amounts = settlement.recipients().stream()
                .map(RewardRecipient::rewardAmount)
                .map(BigInteger::valueOf)
                .map(amount -> amount.multiply(TOKEN_UNIT))
                .toList();

        String transactionHash = rewardDistributorClient.submit(
                batchId,
                rewardDay,
                recipients,
                amounts
        );
        return Optional.of(new RewardTransactionResult(batchId, rewardDay, transactionHash));
    }

    public String createBatchId(LocalDate settlementDate) {
        return Hash.sha3String(BATCH_ID_PREFIX + settlementDate);
    }

    public boolean waitForConfirmation(String transactionHash) {
        return rewardDistributorClient.waitForConfirmation(transactionHash);
    }

    public Optional<String> findTransactionHashByBatchId(String batchId) {
        return rewardDistributorClient.findTransactionHashByBatchId(batchId);
    }

    private void validateSettlement(RewardSettlementResult settlement) {
        long calculatedTotal = 0L;
        for (RewardRecipient recipient : settlement.recipients()) {
            if (recipient.rewardAmount() <= 0L) {
                throw failure("Reward amount must be positive");
            }
            calculatedTotal = Math.addExact(calculatedTotal, recipient.rewardAmount());
        }
        if (calculatedTotal != settlement.totalRewardAmount()) {
            throw failure("Reward settlement total does not match recipients");
        }
    }

    private RewardTransactionException failure(String message) {
        return new RewardTransactionException(new IllegalArgumentException(message));
    }
}
