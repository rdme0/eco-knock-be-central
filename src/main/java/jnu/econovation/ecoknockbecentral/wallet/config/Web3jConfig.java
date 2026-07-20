package jnu.econovation.ecoknockbecentral.wallet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class Web3jConfig {

    @Bean(destroyMethod = "shutdown")
    public Web3j web3j(EthereumSepoliaConfig config) {
        return Web3j.build(new HttpService(config.rpcUrl()));
    }
}
