package jnu.econovation.ecoknockbecentral.reward.client;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import jnu.econovation.ecoknockbecentral.reward.config.RewardTransactionConfig;
import jnu.econovation.ecoknockbecentral.reward.exception.RewardSubmissionUnknownException;
import jnu.econovation.ecoknockbecentral.reward.exception.RewardTransactionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Numeric;

@Component
@RequiredArgsConstructor
public class RewardDistributorClient {

    private static final BigInteger ZERO = BigInteger.ZERO;
    private static final BigInteger GAS_BUFFER_PERCENT = BigInteger.valueOf(120L);
    private static final BigInteger PERCENT_BASE = BigInteger.valueOf(100L);
    private static final Event REWARDS_DISTRIBUTED_EVENT = new Event(
            "RewardsDistributed",
            List.of(
                    new TypeReference<Bytes32>(true) { },
                    new TypeReference<Uint256>(true) { },
                    new TypeReference<Uint256>() { },
                    new TypeReference<Uint256>() { }
            )
    );

    private final Web3j web3j;
    private final RewardTransactionConfig config; // RewardDistributor Contract Address
    private final TransactionManager rewardTransactionManager;
    private final TransactionReceiptProcessor rewardTransactionReceiptProcessor;

    public String submit(
            String batchId,
            BigInteger rewardDay,
            List<String> recipients,
            List<BigInteger> amounts
    ) {
        try {
            Bytes32 encodedBatchId = toBytes32(batchId); // batchId -> Solidity bytes32
            verifyBatchNotProcessed(encodedBatchId);
            verifyOperator(); // checks operator wallet

            // Encodes RewardDistributor(.sol).distributeRewards function
            Function distributeRewards = createDistributeRewardsFunction(
                    encodedBatchId,
                    rewardDay,
                    recipients,
                    amounts
            );
            String encodedFunction = FunctionEncoder.encode(distributeRewards); // ABI Encoding: ( Func name, params, return type ) -> Hex

            simulate(encodedFunction); // Simulates the distribution call to detect a revert before submitting the transaction
            BigInteger gasPrice = getGasPrice();
            BigInteger gasLimit = estimateGas(encodedFunction, gasPrice);

            return submitTransaction(gasPrice, gasLimit, encodedFunction);
        } catch (IOException exception) {
            throw new RewardTransactionException(exception);
        }
    }

    public boolean waitForConfirmation(String transactionHash) {
        try {
            TransactionReceipt receipt = rewardTransactionReceiptProcessor
                    .waitForTransactionReceipt(transactionHash);
            return receipt.isStatusOK();
        } catch (IOException | TransactionException exception) {
            throw new RewardTransactionException(exception);
        }
    }

    public Optional<String> findTransactionHashByBatchId(String batchId) {
        EthFilter filter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                config.distributorAddress()
        );
        filter.addSingleTopic(EventEncoder.encode(REWARDS_DISTRIBUTED_EVENT));
        filter.addSingleTopic(batchId);

        try {
            EthLog response = web3j.ethGetLogs(filter).send();
            ensureSuccessfulResponse(response);
            for (EthLog.LogResult<?> logResult : response.getLogs()) {
                Object value = logResult.get();
                if (value instanceof Log log
                        && log.getTransactionHash() != null
                        && !log.getTransactionHash().isBlank()) {
                    return Optional.of(log.getTransactionHash());
                }
            }
            return Optional.empty();
        } catch (IOException exception) {
            throw new RewardTransactionException(exception);
        }
    }

    private String submitTransaction(
            BigInteger gasPrice,
            BigInteger gasLimit,
            String encodedFunction
    ) {
        EthSendTransaction submitted;
        try {
            submitted = rewardTransactionManager.sendTransaction(
                    gasPrice,
                    gasLimit,
                    config.distributorAddress(),
                    encodedFunction,
                    ZERO
            );
        } catch (IOException exception) {
            throw new RewardSubmissionUnknownException(exception);
        }

        if (submitted.hasError()) {
            throw submissionUnknown(submitted.getError().getMessage());
        }
        String transactionHash = submitted.getTransactionHash();
        if (transactionHash == null || transactionHash.isBlank()) {
            throw submissionUnknown("Missing reward transaction hash");
        }
        return transactionHash;
    }

    private void verifyBatchNotProcessed(Bytes32 batchId) throws IOException {
        Function processedBatches = new Function(
                "processedBatches",
                List.of(batchId),
                List.of(new TypeReference<Bool>() { })
        );
        List<Type> decoded = callView(processedBatches);
        if (decoded.size() != 1 || !(decoded.getFirst() instanceof Bool processed)) {
            throw failure("Invalid processedBatches response");
        }
        if (processed.getValue()) {
            throw failure("Reward batch has already been processed");
        }
    }

    private void verifyOperator() throws IOException {
        Function operator = new Function(
                "operator",
                List.of(),
                List.of(new TypeReference<Address>() { })
        );
        List<Type> decoded = callView(operator);
        if (decoded.size() != 1 || !(decoded.getFirst() instanceof Address configuredOperator)) {
            throw failure("Invalid operator response");
        }

        String signerAddress = rewardTransactionManager.getFromAddress();
        if (!configuredOperator.getValue().equalsIgnoreCase(signerAddress)) {
            throw failure("Configured signer is not the RewardDistributor operator");
        }
    }

    private List<Type> callView(Function function) throws IOException {
        EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(
                        rewardTransactionManager.getFromAddress(),
                        config.distributorAddress(),
                        FunctionEncoder.encode(function)
                ),
                DefaultBlockParameterName.LATEST
        ).send();
        ensureSuccessfulCall(response);
        return FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
    }

    private void simulate(String encodedFunction) throws IOException {
        EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(
                        rewardTransactionManager.getFromAddress(),
                        config.distributorAddress(),
                        encodedFunction
                ),
                DefaultBlockParameterName.LATEST
        ).send();
        ensureSuccessfulCall(response);
    }

    private BigInteger getGasPrice() throws IOException {
        EthGasPrice response = web3j.ethGasPrice().send();
        ensureSuccessfulResponse(response);
        return response.getGasPrice();
    }

    private BigInteger estimateGas(String encodedFunction, BigInteger gasPrice) throws IOException {
        Transaction transaction = Transaction.createFunctionCallTransaction(
                rewardTransactionManager.getFromAddress(),
                null,
                gasPrice,
                null,
                config.distributorAddress(),
                ZERO,
                encodedFunction
        );
        EthEstimateGas response = web3j.ethEstimateGas(transaction).send();
        ensureSuccessfulResponse(response);
        return response.getAmountUsed()
                .multiply(GAS_BUFFER_PERCENT)
                .divide(PERCENT_BASE);
    }

    private Function createDistributeRewardsFunction(
            Bytes32 batchId,
            BigInteger rewardDay,
            List<String> recipients,
            List<BigInteger> amounts
    ) {
        if (recipients.isEmpty() || recipients.size() != amounts.size()) {
            throw failure("Invalid reward recipients and amounts");
        }

        List<Address> encodedRecipients = recipients.stream()
                .map(Address::new)
                .toList();
        List<Uint256> encodedAmounts = amounts.stream()
                .map(Uint256::new)
                .toList();

        return new Function(
                "distributeRewards",
                List.of(
                        batchId,
                        new Uint256(rewardDay),
                        new DynamicArray<>(Address.class, encodedRecipients),
                        new DynamicArray<>(Uint256.class, encodedAmounts)
                ),
                List.of()
        );
    }

    private Bytes32 toBytes32(String batchId) {
        byte[] value = Numeric.hexStringToByteArray(batchId);
        if (value.length != 32) {
            throw failure("Reward batch ID must be 32 bytes");
        }
        return new Bytes32(value);
    }

    private void ensureSuccessfulCall(EthCall response) {
        ensureSuccessfulResponse(response);
        if (response.isReverted()) {
            throw failure(response.getRevertReason());
        }
    }

    private void ensureSuccessfulResponse(Response<?> response) {
        if (response.hasError()) {
            throw failure(response.getError().getMessage());
        }
    }

    private RewardTransactionException failure(String message) {
        return new RewardTransactionException(new IllegalStateException(message));
    }

    private RewardSubmissionUnknownException submissionUnknown(String message) {
        return new RewardSubmissionUnknownException(new IllegalStateException(message));
    }
}
