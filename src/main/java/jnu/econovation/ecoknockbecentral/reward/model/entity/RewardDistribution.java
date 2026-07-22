package jnu.econovation.ecoknockbecentral.reward.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import jnu.econovation.ecoknockbecentral.common.model.entity.BaseEntity;
import jnu.econovation.ecoknockbecentral.reward.model.vo.RewardDistributionStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reward_distribution")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RewardDistribution extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate settlementDate;

    @Column(nullable = false, unique = true, length = 66)
    private String batchId;

    @Column(unique = true, length = 66)
    private String transactionHash;

    @Column(nullable = false)
    private long totalRewardAmount;

    @Column(nullable = false)
    private int recipientCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RewardDistributionStatus status;

    private RewardDistribution(
            LocalDate settlementDate,
            String batchId,
            long totalRewardAmount,
            int recipientCount
    ) {
        this.settlementDate = settlementDate;
        this.batchId = batchId;
        this.totalRewardAmount = totalRewardAmount;
        this.recipientCount = recipientCount;
        this.status = RewardDistributionStatus.PENDING;
    }

    public static RewardDistribution pending(
            LocalDate settlementDate,
            String batchId,
            long totalRewardAmount,
            int recipientCount
    ) {
        if (totalRewardAmount <= 0L || recipientCount <= 0) {
            throw new IllegalArgumentException("Reward distribution must not be empty");
        }
        return new RewardDistribution(
                settlementDate,
                batchId,
                totalRewardAmount,
                recipientCount
        );
    }

    public void markSubmitted(String transactionHash) {
        requireStatus(RewardDistributionStatus.SUBMITTING);
        if (transactionHash == null || transactionHash.isBlank()) {
            throw new IllegalArgumentException("Transaction hash must not be blank");
        }
        this.transactionHash = transactionHash;
        this.status = RewardDistributionStatus.SUBMITTED;
    }

    public void markConfirmed() {
        requireStatus(RewardDistributionStatus.SUBMITTED);
        this.status = RewardDistributionStatus.CONFIRMED;
    }

    public void markFailed() {
        if (status != RewardDistributionStatus.PENDING
                && status != RewardDistributionStatus.SUBMITTING
                && status != RewardDistributionStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Only pending, submitting or submitted distribution can fail"
            );
        }
        this.status = RewardDistributionStatus.FAILED;
    }

    public void markSubmitting(long totalRewardAmount, int recipientCount) {
        if (status != RewardDistributionStatus.PENDING
                && status != RewardDistributionStatus.FAILED) {
            throw new IllegalStateException("Only pending or failed distribution can submit");
        }
        if (totalRewardAmount <= 0L || recipientCount <= 0) {
            throw new IllegalArgumentException("Reward distribution must not be empty");
        }
        this.transactionHash = null;
        this.totalRewardAmount = totalRewardAmount;
        this.recipientCount = recipientCount;
        this.status = RewardDistributionStatus.SUBMITTING;
    }

    private void requireStatus(RewardDistributionStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Reward distribution status must be " + expected + ", but was " + status
            );
        }
    }
}
