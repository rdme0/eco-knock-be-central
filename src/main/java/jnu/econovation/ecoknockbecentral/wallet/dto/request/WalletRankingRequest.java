package jnu.econovation.ecoknockbecentral.wallet.dto.request;

import jnu.econovation.ecoknockbecentral.common.exception.client.BadDataMeaningException;

public record WalletRankingRequest(int limit) {

    public static final int DEFAULT_LIMIT = 3;
    public static final int MIN_LIMIT = 1;
    public static final int MAX_LIMIT = 10;

    public WalletRankingRequest {
        if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
            throw new BadDataMeaningException("limit은 1 이상 10 이하여야 합니다.");
        }
    }
}
