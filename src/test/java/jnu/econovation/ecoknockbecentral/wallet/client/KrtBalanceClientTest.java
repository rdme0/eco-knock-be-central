package jnu.econovation.ecoknockbecentral.wallet.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigInteger;
import jnu.econovation.ecoknockbecentral.wallet.config.EthereumSepoliaConfig;
import jnu.econovation.ecoknockbecentral.wallet.exception.KrtBalanceQueryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.utils.Numeric;

class KrtBalanceClientTest {

    private static final String TOKEN_ADDRESS = "0x052ce2e1310aDF7E2A42B6F87bA2F1d64fE92f30";
    private static final String WALLET_ADDRESS = "0x6813d7a3e620f0c536876b41bfbb56779ea6e7e8";

    private Web3j web3j;
    private Request<?, EthCall> request;
    private KrtBalanceClient client;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        web3j = mock(Web3j.class);
        request = mock(Request.class);
        EthereumSepoliaConfig config = new EthereumSepoliaConfig(
                "https://sepolia.example.test",
                TOKEN_ADDRESS
        );
        client = new KrtBalanceClient(web3j, config);
        doReturn(request).when(web3j).ethCall(
                any(Transaction.class),
                eq(DefaultBlockParameterName.LATEST)
        );
    }

    @Test
    void returnsBalanceFromReadOnlyEthCall() throws IOException {
        BigInteger expected = new BigInteger("13500000000000000001");
        EthCall response = new EthCall();
        response.setResult(Numeric.toHexStringWithPrefixZeroPadded(expected, 64));
        when(request.send()).thenReturn(response);

        BigInteger balance = client.getBalance(WALLET_ADDRESS);

        assertThat(balance).isEqualTo(expected);
    }

    @Test
    void wrapsRpcError() throws IOException {
        EthCall response = new EthCall();
        response.setError(new Response.Error(-32000, "rpc failed"));
        when(request.send()).thenReturn(response);

        assertThatThrownBy(() -> client.getBalance(WALLET_ADDRESS))
                .isInstanceOf(KrtBalanceQueryException.class)
                .hasRootCauseMessage("rpc failed");
    }

    @Test
    void wrapsRevertedCall() throws IOException {
        EthCall response = mock(EthCall.class);
        when(response.isReverted()).thenReturn(true);
        when(response.getRevertReason()).thenReturn("execution reverted");
        when(request.send()).thenReturn(response);

        assertThatThrownBy(() -> client.getBalance(WALLET_ADDRESS))
                .isInstanceOf(KrtBalanceQueryException.class)
                .hasRootCauseMessage("execution reverted");
    }

    @Test
    void wrapsIoFailure() throws IOException {
        IOException failure = new IOException("network unavailable");
        when(request.send()).thenThrow(failure);

        assertThatThrownBy(() -> client.getBalance(WALLET_ADDRESS))
                .isInstanceOf(KrtBalanceQueryException.class)
                .hasCause(failure);
    }
}
