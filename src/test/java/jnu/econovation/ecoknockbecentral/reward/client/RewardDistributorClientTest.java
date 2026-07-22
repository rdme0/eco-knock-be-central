package jnu.econovation.ecoknockbecentral.reward.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import jnu.econovation.ecoknockbecentral.reward.config.RewardTransactionConfig;
import jnu.econovation.ecoknockbecentral.reward.exception.RewardSubmissionUnknownException;
import jnu.econovation.ecoknockbecentral.reward.exception.RewardTransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Numeric;

class RewardDistributorClientTest {

    private static final String DISTRIBUTOR_ADDRESS = "0x1398e75da0a95f2a6c65a1dfb002d8c3af3db23d";
    private static final String OPERATOR_ADDRESS = "0x6813d7a3e620f0c536876b41bfbb56779ea6e7e8";
    private static final String OTHER_ADDRESS = "0x1111111111111111111111111111111111111111";
    private static final String RECIPIENT_ADDRESS = "0x2222222222222222222222222222222222222222";
    private static final String BATCH_ID = "0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private static final String TRANSACTION_HASH = "0xbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
    private static final BigInteger REWARD_DAY = new BigInteger("20260721");
    private static final BigInteger AMOUNT = BigInteger.TEN.pow(18);
    private static final BigInteger GAS_PRICE = BigInteger.valueOf(2_000_000_000L);
    private static final BigInteger ESTIMATED_GAS = BigInteger.valueOf(100_000L);

    private Web3j web3j;
    private TransactionManager transactionManager;
    private TransactionReceiptProcessor receiptProcessor;
    private Request<?, EthCall> ethCallRequest;
    private Request<?, EthGasPrice> gasPriceRequest;
    private Request<?, EthEstimateGas> estimateGasRequest;
    private Request<?, EthLog> logsRequest;
    private RewardDistributorClient client;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        web3j = mock(Web3j.class);
        transactionManager = mock(TransactionManager.class);
        receiptProcessor = mock(TransactionReceiptProcessor.class);
        ethCallRequest = mock(Request.class);
        gasPriceRequest = mock(Request.class);
        estimateGasRequest = mock(Request.class);
        logsRequest = mock(Request.class);
        RewardTransactionConfig config = new RewardTransactionConfig(DISTRIBUTOR_ADDRESS);
        client = new RewardDistributorClient(
                web3j,
                config,
                transactionManager,
                receiptProcessor
        );

        when(transactionManager.getFromAddress()).thenReturn(OPERATOR_ADDRESS);
        doReturn(ethCallRequest).when(web3j).ethCall(
                any(Transaction.class),
                eq(DefaultBlockParameterName.LATEST)
        );
        doReturn(gasPriceRequest).when(web3j).ethGasPrice();
        doReturn(estimateGasRequest).when(web3j).ethEstimateGas(any(Transaction.class));
        doReturn(logsRequest).when(web3j).ethGetLogs(any(EthFilter.class));
    }

    @Test
    void submitsTransactionAndReturnsHash() throws Exception {
        prepareSuccessfulReadAndSimulation();
        prepareGasResponses();
        EthSendTransaction submitted = successfulSubmission();
        when(transactionManager.sendTransaction(
                eq(GAS_PRICE),
                eq(BigInteger.valueOf(120_000L)),
                eq(DISTRIBUTOR_ADDRESS),
                any(String.class),
                eq(BigInteger.ZERO)
        )).thenReturn(submitted);

        String transactionHash = submit();

        assertThat(transactionHash).isEqualTo(TRANSACTION_HASH);
        verify(receiptProcessor, never()).waitForTransactionReceipt(any());
    }

    @Test
    void rejectsAlreadyProcessedBatch() throws IOException {
        EthCall processed = ethCall(uint256Result(BigInteger.ONE));
        when(ethCallRequest.send()).thenReturn(processed);

        assertThatThrownBy(this::submit)
                .isInstanceOf(RewardTransactionException.class)
                .hasRootCauseMessage("Reward batch has already been processed");
        verify(transactionManager, never()).sendTransaction(any(), any(), any(), any(), any());
    }

    @Test
    void rejectsSignerThatIsNotOperator() throws IOException {
        EthCall processed = ethCall(uint256Result(BigInteger.ZERO));
        EthCall operator = ethCall(addressResult(OTHER_ADDRESS));
        when(ethCallRequest.send()).thenReturn(processed, operator);

        assertThatThrownBy(this::submit)
                .isInstanceOf(RewardTransactionException.class)
                .hasRootCauseMessage("Configured signer is not the RewardDistributor operator");
        verify(transactionManager, never()).sendTransaction(any(), any(), any(), any(), any());
    }

    @Test
    void wrapsSimulationRevert() throws IOException {
        EthCall processed = ethCall(uint256Result(BigInteger.ZERO));
        EthCall operator = ethCall(addressResult(OPERATOR_ADDRESS));
        EthCall simulation = mock(EthCall.class);
        when(simulation.isReverted()).thenReturn(true);
        when(simulation.getRevertReason()).thenReturn("execution reverted");
        when(ethCallRequest.send()).thenReturn(processed, operator, simulation);

        assertThatThrownBy(this::submit)
                .isInstanceOf(RewardTransactionException.class)
                .hasRootCauseMessage("execution reverted");
        verify(transactionManager, never()).sendTransaction(any(), any(), any(), any(), any());
    }

    @Test
    void wrapsRpcError() throws IOException {
        EthCall processed = new EthCall();
        processed.setError(new Response.Error(-32000, "rpc failed"));
        when(ethCallRequest.send()).thenReturn(processed);

        assertThatThrownBy(this::submit)
                .isInstanceOf(RewardTransactionException.class)
                .hasRootCauseMessage("rpc failed");
    }

    @Test
    void treatsSubmissionTransportErrorAsUnknown() throws Exception {
        prepareSuccessfulReadAndSimulation();
        prepareGasResponses();
        IOException responseLost = new IOException("submission response lost");
        when(transactionManager.sendTransaction(any(), any(), any(), any(), any()))
                .thenThrow(responseLost);

        assertThatThrownBy(this::submit)
                .isInstanceOf(RewardSubmissionUnknownException.class)
                .hasCause(responseLost);
    }

    @Test
    void findsTransactionHashFromDistributedEvent() throws IOException {
        EthLog.LogObject log = new EthLog.LogObject();
        log.setTransactionHash(TRANSACTION_HASH);
        EthLog response = new EthLog();
        response.setResult(List.of(log));
        when(logsRequest.send()).thenReturn(response);

        assertThat(client.findTransactionHashByBatchId(BATCH_ID))
                .contains(TRANSACTION_HASH);
    }

    @Test
    void returnsTrueWhenReceiptIsSuccessful() throws Exception {
        when(receiptProcessor.waitForTransactionReceipt(TRANSACTION_HASH))
                .thenReturn(receipt("0x1"));

        boolean confirmed = client.waitForConfirmation(TRANSACTION_HASH);

        assertThat(confirmed).isTrue();
    }

    @Test
    void returnsFalseWhenReceiptIsReverted() throws Exception {
        when(receiptProcessor.waitForTransactionReceipt(TRANSACTION_HASH))
                .thenReturn(receipt("0x0"));

        boolean confirmed = client.waitForConfirmation(TRANSACTION_HASH);

        assertThat(confirmed).isFalse();
    }

    @Test
    void wrapsReceiptPollingError() throws Exception {
        IOException pollingFailure = new IOException("receipt polling failed");
        when(receiptProcessor.waitForTransactionReceipt(TRANSACTION_HASH))
                .thenThrow(pollingFailure);

        assertThatThrownBy(() -> client.waitForConfirmation(TRANSACTION_HASH))
                .isInstanceOf(RewardTransactionException.class)
                .hasCause(pollingFailure);
    }

    private String submit() {
        return client.submit(
                BATCH_ID,
                REWARD_DAY,
                List.of(RECIPIENT_ADDRESS),
                List.of(AMOUNT)
        );
    }

    private void prepareSuccessfulReadAndSimulation() throws IOException {
        EthCall processed = ethCall(uint256Result(BigInteger.ZERO));
        EthCall operator = ethCall(addressResult(OPERATOR_ADDRESS));
        EthCall simulation = ethCall("0x");
        when(ethCallRequest.send()).thenReturn(processed, operator, simulation);
    }

    private void prepareGasResponses() throws IOException {
        EthGasPrice gasPrice = new EthGasPrice();
        gasPrice.setResult(Numeric.encodeQuantity(GAS_PRICE));
        when(gasPriceRequest.send()).thenReturn(gasPrice);

        EthEstimateGas estimateGas = new EthEstimateGas();
        estimateGas.setResult(Numeric.encodeQuantity(ESTIMATED_GAS));
        when(estimateGasRequest.send()).thenReturn(estimateGas);
    }

    private EthSendTransaction successfulSubmission() {
        EthSendTransaction submitted = new EthSendTransaction();
        submitted.setResult(TRANSACTION_HASH);
        return submitted;
    }

    private TransactionReceipt receipt(String status) {
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTransactionHash(TRANSACTION_HASH);
        receipt.setStatus(status);
        return receipt;
    }

    private EthCall ethCall(String result) {
        EthCall response = new EthCall();
        response.setResult(result);
        return response;
    }

    private String uint256Result(BigInteger value) {
        return Numeric.toHexStringWithPrefixZeroPadded(value, 64);
    }

    private String addressResult(String address) {
        return "0x" + "0".repeat(24) + Numeric.cleanHexPrefix(address);
    }
}
