package jnu.econovation.ecoknockbecentral.wallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

public record WalletRankingResponse(
        @Schema(description = "토큰 심볼", example = "KRT")
        String symbol,
        @Schema(description = "랭킹 계산 시각", example = "2026-07-23T01:00:00Z")
        Instant calculatedAt,
        @Schema(description = "KRT 보유량 랭킹")
        List<WalletRankingEntryResponse> rankings
) {
}
