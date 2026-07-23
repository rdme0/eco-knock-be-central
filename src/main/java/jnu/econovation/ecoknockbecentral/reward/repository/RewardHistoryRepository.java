package jnu.econovation.ecoknockbecentral.reward.repository;

import java.time.Instant;
import jnu.econovation.ecoknockbecentral.reward.model.entity.RewardHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RewardHistoryRepository extends JpaRepository<RewardHistory, Long> {

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO reward_history (
                created_at, updated_at, member_id, distribution_id,
                reward_type, reward_amount, stay_hours, wallet_address
            ) VALUES (
                :createdAt, :updatedAt, :memberId, :distributionId,
                :rewardType, :rewardAmount, :stayHours, :walletAddress
            )
            ON CONFLICT (distribution_id, member_id, reward_type)
            DO UPDATE SET
                reward_amount = EXCLUDED.reward_amount,
                stay_hours = EXCLUDED.stay_hours,
                wallet_address = EXCLUDED.wallet_address,
                updated_at = EXCLUDED.updated_at
            """, nativeQuery = true)
    void upsert(
            @Param("createdAt") Instant createdAt,
            @Param("updatedAt") Instant updatedAt,
            @Param("memberId") Long memberId,
            @Param("distributionId") Long distributionId,
            @Param("rewardType") String rewardType,
            @Param("rewardAmount") long rewardAmount,
            @Param("stayHours") Integer stayHours,
            @Param("walletAddress") String walletAddress
    );

    @Query("""
            SELECT history
            FROM RewardHistory history
            WHERE history.member.id = :memberId
              AND history.rewardDistribution.status = jnu.econovation.ecoknockbecentral.reward.model.vo.RewardDistributionStatus.CONFIRMED
            ORDER BY history.rewardDistribution.settlementDate DESC,
                     history.rewardType ASC,
                     history.id ASC
            """)
    Page<RewardHistory> findConfirmedByMemberId(
            @Param("memberId") Long memberId,
            Pageable pageable
    );
}
