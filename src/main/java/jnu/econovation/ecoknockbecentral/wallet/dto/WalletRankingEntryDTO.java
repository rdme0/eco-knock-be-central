package jnu.econovation.ecoknockbecentral.wallet.dto;

import jnu.econovation.ecoknockbecentral.wallet.dto.response.WalletRankingEntryResponse;

public record WalletRankingEntryDTO(
        int rank,
        Long memberId,
        String memberName,
        int cohort,
        String balance
) {

    public WalletRankingEntryResponse toResponse() {
        return new WalletRankingEntryResponse(rank, memberId, memberName, cohort, balance);
    }
}
