package jnu.econovation.ecoknockbecentral.reward.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import jnu.econovation.ecoknockbecentral.reward.model.vo.RewardDistributionStatus;
import org.junit.jupiter.api.Test;

class RewardDistributionTest {

    private static final LocalDate SETTLEMENT_DATE = LocalDate.of(2026, 7, 21);
    private static final String BATCH_ID =
            "0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private static final String TRANSACTION_HASH =
            "0xbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";

    @Test
    void createsPendingDistribution() {
        RewardDistribution distribution = createPending();

        assertThat(distribution.getSettlementDate()).isEqualTo(SETTLEMENT_DATE);
        assertThat(distribution.getBatchId()).isEqualTo(BATCH_ID);
        assertThat(distribution.getTotalRewardAmount()).isEqualTo(12L);
        assertThat(distribution.getRecipientCount()).isEqualTo(2);
        assertThat(distribution.getStatus()).isEqualTo(RewardDistributionStatus.PENDING);
        assertThat(distribution.getTransactionHash()).isNull();
    }

    @Test
    void transitionsFromPendingToSubmittingToSubmittedAndConfirmed() {
        RewardDistribution distribution = createPending();

        distribution.markSubmitting(12L, 2);
        assertThat(distribution.getStatus()).isEqualTo(RewardDistributionStatus.SUBMITTING);

        distribution.markSubmitted(TRANSACTION_HASH);
        assertThat(distribution.getStatus()).isEqualTo(RewardDistributionStatus.SUBMITTED);
        assertThat(distribution.getTransactionHash()).isEqualTo(TRANSACTION_HASH);

        distribution.markConfirmed();
        assertThat(distribution.getStatus()).isEqualTo(RewardDistributionStatus.CONFIRMED);
    }

    @Test
    void preparesFailedDistributionForRetry() {
        RewardDistribution distribution = createPending();
        distribution.markSubmitting(12L, 2);
        distribution.markSubmitted(TRANSACTION_HASH);
        distribution.markFailed();

        distribution.markSubmitting(20L, 3);

        assertThat(distribution.getStatus()).isEqualTo(RewardDistributionStatus.SUBMITTING);
        assertThat(distribution.getTransactionHash()).isNull();
        assertThat(distribution.getTotalRewardAmount()).isEqualTo(20L);
        assertThat(distribution.getRecipientCount()).isEqualTo(3);
    }

    @Test
    void rejectsInvalidStatusTransition() {
        RewardDistribution distribution = createPending();

        assertThatThrownBy(distribution::markConfirmed)
                .isInstanceOf(IllegalStateException.class);
    }

    private RewardDistribution createPending() {
        return RewardDistribution.pending(SETTLEMENT_DATE, BATCH_ID, 12L, 2);
    }
}
