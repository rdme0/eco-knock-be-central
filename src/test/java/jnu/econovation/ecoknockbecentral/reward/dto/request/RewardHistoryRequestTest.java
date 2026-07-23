package jnu.econovation.ecoknockbecentral.reward.dto.request;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jnu.econovation.ecoknockbecentral.common.exception.client.BadDataMeaningException;
import org.junit.jupiter.api.Test;

class RewardHistoryRequestTest {

    @Test
    void acceptsDefaultsAndMaximumSize() {
        new RewardHistoryRequest(0, 20);
        new RewardHistoryRequest(3, 100);
    }

    @Test
    void rejectsNegativePageAndInvalidSize() {
        assertThatThrownBy(() -> new RewardHistoryRequest(-1, 20))
                .isInstanceOf(BadDataMeaningException.class);
        assertThatThrownBy(() -> new RewardHistoryRequest(0, 0))
                .isInstanceOf(BadDataMeaningException.class);
        assertThatThrownBy(() -> new RewardHistoryRequest(0, 101))
                .isInstanceOf(BadDataMeaningException.class);
    }
}
