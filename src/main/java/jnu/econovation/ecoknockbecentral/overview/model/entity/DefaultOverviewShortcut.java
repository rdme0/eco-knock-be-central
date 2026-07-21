package jnu.econovation.ecoknockbecentral.overview.model.entity;

import jakarta.persistence.*;
import jnu.econovation.ecoknockbecentral.common.model.entity.BaseEntity;
import jnu.econovation.ecoknockbecentral.overview.model.vo.ValidHttpUrl;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DefaultOverviewShortcut extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @AttributeOverride(name = "value", column = @Column(name = "icon_url", length = 2048))
    private ValidHttpUrl iconUrl;

    @AttributeOverride(name = "value", column = @Column(name = "target_url", nullable = false, length = 2048))
    private ValidHttpUrl targetUrl;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false, length = 2048)
    private String name;

    @Builder
    DefaultOverviewShortcut(ValidHttpUrl iconUrl, ValidHttpUrl targetUrl, Integer sortOrder, String name) {
        this.iconUrl = iconUrl;
        this.targetUrl = targetUrl;
        this.sortOrder = sortOrder;
        this.name = name;
    }
}
