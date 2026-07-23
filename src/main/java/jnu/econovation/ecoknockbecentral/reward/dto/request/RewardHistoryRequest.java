package jnu.econovation.ecoknockbecentral.reward.dto.request;

import jnu.econovation.ecoknockbecentral.common.exception.client.BadDataMeaningException;

public record RewardHistoryRequest(int page, int size) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public RewardHistoryRequest {
        if (page < 0) {
            throw new BadDataMeaningException("page는 0 이상이어야 합니다.");
        }
        if (size <= 0 || size > MAX_SIZE) {
            throw new BadDataMeaningException("size는 1 이상 100 이하여야 합니다.");
        }
    }
}
