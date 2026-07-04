package jnu.econovation.ecoknockbecentral.control.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jnu.econovation.ecoknockbecentral.common.model.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ControlSetting extends BaseEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private double darkLuxThreshold;

    @Column(nullable = false)
    private double brightLuxThreshold;

    @Column(nullable = false)
    private int darkDetectionTimeThreshold;

    @Column(nullable = false)
    private int brightDetectionTimeThreshold;

    @Column(nullable = false)
    private int airQualityDetectionTimeThreshold;

    @Column(nullable = false)
    private int badAirQualityRatioThreshold;

    @Column(nullable = false)
    private int cleanAirQualityRatioThreshold;

    @Column(nullable = false)
    private int cooldownMinutes;

    public ControlSetting(
            Long id,
            boolean enabled,
            double darkLuxThreshold,
            double brightLuxThreshold,
            int darkDetectionTimeThreshold,
            int brightDetectionTimeThreshold,
            int airQualityDetectionTimeThreshold,
            int badAirQualityRatioThreshold,
            int cleanAirQualityRatioThreshold,
            int cooldownMinutes
    ) {
        this.id = id;
        this.enabled = enabled;
        this.darkLuxThreshold = darkLuxThreshold;
        this.brightLuxThreshold = brightLuxThreshold;
        this.darkDetectionTimeThreshold = darkDetectionTimeThreshold;
        this.brightDetectionTimeThreshold = brightDetectionTimeThreshold;
        this.airQualityDetectionTimeThreshold = airQualityDetectionTimeThreshold;
        this.badAirQualityRatioThreshold = badAirQualityRatioThreshold;
        this.cleanAirQualityRatioThreshold = cleanAirQualityRatioThreshold;
        this.cooldownMinutes = cooldownMinutes;
    }

    public void update(
            boolean enabled,
            double darkLuxThreshold,
            double brightLuxThreshold,
            int darkDetectionTimeThreshold,
            int brightDetectionTimeThreshold,
            int airQualityDetectionTimeThreshold,
            int badAirQualityRatioThreshold,
            int cleanAirQualityRatioThreshold,
            int cooldownMinutes
    ) {
        this.enabled = enabled;
        this.darkLuxThreshold = darkLuxThreshold;
        this.brightLuxThreshold = brightLuxThreshold;
        this.darkDetectionTimeThreshold = darkDetectionTimeThreshold;
        this.brightDetectionTimeThreshold = brightDetectionTimeThreshold;
        this.airQualityDetectionTimeThreshold = airQualityDetectionTimeThreshold;
        this.badAirQualityRatioThreshold = badAirQualityRatioThreshold;
        this.cleanAirQualityRatioThreshold = cleanAirQualityRatioThreshold;
        this.cooldownMinutes = cooldownMinutes;
    }

    public void updateEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
