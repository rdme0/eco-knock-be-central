package jnu.econovation.ecoknockbecentral.wallet.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jnu.econovation.ecoknockbecentral.common.model.entity.BaseEntity;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.wallet.model.vo.WalletType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_wallet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberWallet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, unique = true, length = 42)
    private String walletAddress;

    @Column(columnDefinition = "TEXT")
    private String encryptedPrivateKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WalletType walletType;

    @Column(name = "is_active", nullable = false)
    private boolean activeRewardDestination;

    private MemberWallet(
            Member member,
            String walletAddress,
            String encryptedPrivateKey,
            WalletType walletType,
            boolean activeRewardDestination
    ) {
        this.member = member;
        this.walletAddress = walletAddress;
        this.encryptedPrivateKey = encryptedPrivateKey;
        this.walletType = walletType;
        this.activeRewardDestination = activeRewardDestination;
    }

    public static MemberWallet managed(
            Member member,
            String walletAddress,
            String encryptedPrivateKey,
            boolean activeRewardDestination
    ) {
        return new MemberWallet(
                member,
                walletAddress,
                encryptedPrivateKey,
                WalletType.MANAGED,
                activeRewardDestination
        );
    }
}
