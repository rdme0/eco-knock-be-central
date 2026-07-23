package jnu.econovation.ecoknockbecentral.wallet.dto.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jnu.econovation.ecoknockbecentral.common.exception.client.BadDataMeaningException;
import org.junit.jupiter.api.Test;

class WalletRankingRequestTest {

    @Test
    void usesThreeAsDocumentedDefault() {
        assertThat(WalletRankingRequest.DEFAULT_LIMIT).isEqualTo(3);
    }

    @Test
    void acceptsInclusiveLimitBounds() {
        assertThat(new WalletRankingRequest(1).limit()).isEqualTo(1);
        assertThat(new WalletRankingRequest(10).limit()).isEqualTo(10);
    }

    @Test
    void rejectsLimitOutsideRange() {
        assertThatThrownBy(() -> new WalletRankingRequest(0))
                .isInstanceOf(BadDataMeaningException.class);
        assertThatThrownBy(() -> new WalletRankingRequest(11))
                .isInstanceOf(BadDataMeaningException.class);
    }
}
