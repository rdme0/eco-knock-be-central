package jnu.econovation.ecoknockbecentral.wallet.repository;

import java.util.Optional;
import jnu.econovation.ecoknockbecentral.wallet.model.entity.MemberWallet;
import jnu.econovation.ecoknockbecentral.wallet.model.vo.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberWalletRepository extends JpaRepository<MemberWallet, Long> {

    Optional<MemberWallet> findByMemberIdAndWalletType(Long memberId, WalletType walletType);

    Optional<MemberWallet> findByMemberIdAndActiveRewardDestinationTrue(Long memberId);

    boolean existsByMemberIdAndActiveRewardDestinationTrue(Long memberId);
}
