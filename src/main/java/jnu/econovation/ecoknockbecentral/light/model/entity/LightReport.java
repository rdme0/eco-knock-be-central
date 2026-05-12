package jnu.econovation.ecoknockbecentral.light.model.entity;

import jakarta.persistence.*;
import jnu.econovation.ecoknockbecentral.common.model.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LightReport extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double lux;

    @Column(nullable = false)
    private Instant measuredAt;

    @Column(nullable = false)
    private Integer rawAls;

    @Column(nullable = false)
    private Integer rawWhite;

    @Builder
    LightReport(
            Double lux,
            Instant measuredAt,
            Integer rawAls,
            Integer rawWhite
    ) {
        this.lux = lux;
        this.measuredAt = measuredAt;
        this.rawAls = rawAls;
        this.rawWhite = rawWhite;
    }
}
