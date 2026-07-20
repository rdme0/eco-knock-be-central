package jnu.econovation.ecoknockbecentral.wallet.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import jnu.econovation.ecoknockbecentral.wallet.client.KrtBalanceClient;
import jnu.econovation.ecoknockbecentral.wallet.dto.response.WalletBalanceResponse;
import jnu.econovation.ecoknockbecentral.wallet.exception.WalletNotFoundException;
import jnu.econovation.ecoknockbecentral.wallet.model.entity.MemberWallet;
import jnu.econovation.ecoknockbecentral.wallet.repository.MemberWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletBalanceService {

    private static final int KRT_DECIMALS = 18;
    private static final String KRT_SYMBOL = "KRT";

    private final MemberWalletRepository memberWalletRepository;
    private final KrtBalanceClient krtBalanceClient;

    public WalletBalanceResponse getWalletBalance(Long memberId) {
        MemberWallet wallet = memberWalletRepository
                .findByMemberIdAndActiveRewardDestinationTrue(memberId)
                .orElseThrow(WalletNotFoundException::new);

        BigInteger rawBalance = krtBalanceClient.getBalance(wallet.getWalletAddress());
        return new WalletBalanceResponse(
                wallet.getWalletAddress(),
                wallet.getWalletType(),
                formatBalance(rawBalance),
                KRT_SYMBOL
        );
    }

    private String formatBalance(BigInteger rawBalance) {
        return new BigDecimal(rawBalance, KRT_DECIMALS)
                .stripTrailingZeros()
                .toPlainString();
    }
}
