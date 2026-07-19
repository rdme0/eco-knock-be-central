package jnu.econovation.ecoknockbecentral.wallet.client;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import jnu.econovation.ecoknockbecentral.wallet.config.EthereumSepoliaConfig;
import jnu.econovation.ecoknockbecentral.wallet.exception.KrtBalanceQueryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

@Component
@RequiredArgsConstructor
public class KrtBalanceClient {

    private final Web3j web3j;
    private final EthereumSepoliaConfig config;

    public BigInteger getBalance(String walletAddress) {
        Function balanceOf = new Function(
                "balanceOf",
                List.of(new Address(walletAddress)),
                List.of(new TypeReference<Uint256>() { })
        );

        try {
            EthCall response = web3j.ethCall(
                    Transaction.createEthCallTransaction(
                            null,
                            config.krtTokenAddress(),
                            FunctionEncoder.encode(balanceOf)
                    ),
                    DefaultBlockParameterName.LATEST
            ).send();

            if (response.hasError()) {
                throw queryFailure(response.getError().getMessage());
            }
            if (response.isReverted()) {
                throw queryFailure(response.getRevertReason());
            }

            List<Type> decoded = FunctionReturnDecoder.decode(
                    response.getValue(),
                    balanceOf.getOutputParameters()
            );
            if (decoded.size() != 1 || !(decoded.getFirst() instanceof Uint256 balance)) {
                throw queryFailure("Invalid balanceOf response");
            }
            return balance.getValue();
        } catch (IOException exception) {
            throw new KrtBalanceQueryException(exception);
        }
    }

    private KrtBalanceQueryException queryFailure(String message) {
        return new KrtBalanceQueryException(new IllegalStateException(message));
    }
}
