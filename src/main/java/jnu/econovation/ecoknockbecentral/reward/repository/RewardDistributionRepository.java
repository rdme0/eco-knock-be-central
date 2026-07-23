package jnu.econovation.ecoknockbecentral.reward.repository;

import java.util.Collection;
import java.util.Optional;
import jnu.econovation.ecoknockbecentral.reward.model.entity.RewardDistribution;
import jnu.econovation.ecoknockbecentral.reward.model.vo.RewardDistributionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RewardDistributionRepository extends JpaRepository<RewardDistribution, Long> {

    Optional<RewardDistribution> findByBatchId(String batchId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            UPDATE RewardDistribution distribution
            SET distribution.status = :submittingStatus,
                distribution.transactionHash = NULL,
                distribution.totalRewardAmount = :totalRewardAmount,
                distribution.recipientCount = :recipientCount
            WHERE distribution.id = :id
              AND distribution.status IN :claimableStatuses
            """)
    int claimForSubmission(
            @Param("id") Long id,
            @Param("totalRewardAmount") long totalRewardAmount,
            @Param("recipientCount") int recipientCount,
            @Param("submittingStatus") RewardDistributionStatus submittingStatus,
            @Param("claimableStatuses") Collection<RewardDistributionStatus> claimableStatuses
    );
}
