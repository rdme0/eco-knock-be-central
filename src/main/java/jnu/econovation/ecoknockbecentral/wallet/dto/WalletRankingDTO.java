package jnu.econovation.ecoknockbecentral.wallet.dto;

import java.time.Instant;
import java.util.List;
import jnu.econovation.ecoknockbecentral.wallet.dto.response.WalletRankingResponse;

public record WalletRankingDTO(
        String symbol,
        Instant calculatedAt,
        List<WalletRankingEntryDTO> rankings
) {

    public WalletRankingDTO {
        rankings = List.copyOf(rankings);
    }

    public WalletRankingResponse toResponse(int limit) {
        return new WalletRankingResponse(
                symbol,
                calculatedAt,
                rankings.stream()
                        .limit(limit)
                        .map(WalletRankingEntryDTO::toResponse)
                        .toList()
        );
    }
}
