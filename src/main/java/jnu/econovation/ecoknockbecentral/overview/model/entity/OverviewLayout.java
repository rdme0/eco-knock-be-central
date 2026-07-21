package jnu.econovation.ecoknockbecentral.overview.model.entity;

import jakarta.persistence.*;
import jnu.econovation.ecoknockbecentral.common.model.entity.BaseEntity;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import jnu.econovation.ecoknockbecentral.overview.model.vo.GridSize;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OverviewLayout extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "grid_size", nullable = false))
    private GridSize gridSize;

    @Builder
    OverviewLayout(Member member, GridSize gridSize) {
        this.member = member;
        this.gridSize = gridSize;
    }

    public void changeGridSize(GridSize gridSize) {
        this.gridSize = gridSize;
    }
}
