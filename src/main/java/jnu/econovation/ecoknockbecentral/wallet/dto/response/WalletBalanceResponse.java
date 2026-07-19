package jnu.econovation.ecoknockbecentral.wallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jnu.econovation.ecoknockbecentral.wallet.model.vo.WalletType;

public record WalletBalanceResponse(
        @Schema(description = "활성 보상 지갑 주소", example = "0x6813d7a3e620f0c536876b41bfbb56779ea6e7e8")
        String walletAddress,

        @Schema(description = "지갑 유형", example = "MANAGED")
        WalletType walletType,

        @Schema(description = "사람이 읽을 수 있는 KRT 잔액", example = "13.5")
        String balance,

        @Schema(description = "토큰 심볼", example = "KRT")
        String symbol
) {
}
