package jnu.econovation.ecoknockbecentral.wallet.repository;

import java.util.List;
import java.util.Optional;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import jnu.econovation.ecoknockbecentral.wallet.model.entity.MemberWallet;
import jnu.econovation.ecoknockbecentral.wallet.model.vo.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberWalletRepository extends JpaRepository<MemberWallet, Long> {

    Optional<MemberWallet> findByMemberIdAndWalletType(Long memberId, WalletType walletType);

    Optional<MemberWallet> findByMemberIdAndActiveRewardDestinationTrue(Long memberId);

    boolean existsByMemberIdAndActiveRewardDestinationTrue(Long memberId);

    @Query("""
            select wallet
            from MemberWallet wallet
            join fetch wallet.member member
            where wallet.activeRewardDestination = true
              and member.role <> :guestRole
            """)
    List<MemberWallet> findActiveRewardDestinationsExcludingRole(@Param("guestRole") Role guestRole);
}
