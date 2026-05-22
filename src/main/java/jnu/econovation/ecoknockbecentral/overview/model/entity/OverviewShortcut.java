package jnu.econovation.ecoknockbecentral.overview.model.entity;

import jakarta.persistence.*;
import jnu.econovation.ecoknockbecentral.common.model.entity.BaseEntity;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.overview.model.vo.ValidHttpUrl;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OverviewShortcut extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @AttributeOverride(name = "value", column = @Column(name = "icon_url", nullable = false, length = 2048))
    private ValidHttpUrl iconUrl;

    @AttributeOverride(name = "value", column = @Column(name = "target_url", nullable = false, length = 2048))
    private ValidHttpUrl targetUrl;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private String name;

    @Builder
    OverviewShortcut(Member member, ValidHttpUrl iconUrl, ValidHttpUrl targetUrl, Integer sortOrder, String name) {
        this.member = member;
        this.iconUrl = iconUrl;
        this.targetUrl = targetUrl;
        this.sortOrder = sortOrder;
        this.name = name;
    }
}