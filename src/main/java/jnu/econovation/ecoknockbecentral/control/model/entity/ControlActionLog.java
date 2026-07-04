package jnu.econovation.ecoknockbecentral.control.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jnu.econovation.ecoknockbecentral.common.model.entity.BaseEntity;
import jnu.econovation.ecoknockbecentral.control.model.vo.ControlDecision;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ControlActionLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant actedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ControlDecision decision;

    @Column(nullable = false, columnDefinition = "text")
    private String reason;

    @Builder
    ControlActionLog(
            Instant actedAt,
            ControlDecision decision,
            String reason
    ) {
        this.actedAt = actedAt;
        this.decision = decision;
        this.reason = reason;
    }
}
