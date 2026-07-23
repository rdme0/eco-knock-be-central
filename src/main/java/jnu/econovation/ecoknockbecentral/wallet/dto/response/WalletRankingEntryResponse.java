package jnu.econovation.ecoknockbecentral.wallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record WalletRankingEntryResponse(
        @Schema(description = "랭킹", example = "1")
        int rank,
        @Schema(description = "회원 ID", example = "7")
        Long memberId,
        @Schema(description = "회원 이름", example = "eco-member")
        String memberName,
        @Schema(description = "기수", example = "32")
        int cohort,
        @Schema(description = "보유 KRT 잔액", example = "13.5")
        String balance
) {
}
