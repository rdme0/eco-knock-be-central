package jnu.econovation.ecoknockbecentral.member.model.entity;

import jakarta.persistence.*;
import jnu.econovation.ecoknockbecentral.common.converter.StringEncryptConverter;
import jnu.econovation.ecoknockbecentral.common.model.entity.BaseEntity;
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus;
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort;
import jnu.econovation.ecoknockbecentral.member.model.vo.Role;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long ssoMemberId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "cohort"))
    private Cohort cohort;

    @Column(nullable = false)
    @Convert(converter = StringEncryptConverter.class)
    private String name;

    @Enumerated(EnumType.STRING)
    private ActiveStatus status;

    @Column(name = "guest_expires_at")
    private Instant guestExpiresAt;

    @Builder
    Member(
            Long ssoMemberId,
            Cohort cohort,
            String name,
            ActiveStatus status
    ) {
        this.ssoMemberId = ssoMemberId;
        this.role = Role.USER;
        this.cohort = cohort;
        this.name = name;
        this.status = status;
    }

    public static Member createGuest(Instant guestExpiresAt) {
        Member member = new Member();
        member.role = Role.GUEST;
        member.name = "게스트";
        member.guestExpiresAt = guestExpiresAt;
        return member;
    }

    public void promoteToAdmin() {
        this.role = Role.ADMIN;
    }
}
