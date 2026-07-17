package jnu.econovation.ecoknockbecentral.ai.model.entity;

import jakarta.persistence.*;
import jnu.econovation.ecoknockbecentral.ai.dto.aiserver.RawAIServerResponseDTO;
import jnu.econovation.ecoknockbecentral.common.model.entity.BaseEntity;
import jnu.econovation.ecoknockbecentral.member.model.entity.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ai_chat_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AIChatHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    // 아래는 기록용 필드 (로깅 및 디버깅 할때 용이)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private RawAIServerResponseDTO rawResponse;

    @Builder
    AIChatHistory(
            Member member,
            String question,
            String answer,
            RawAIServerResponseDTO rawResponse
    ) {
        this.member = member;
        this.question = question;
        this.answer = answer;
        this.rawResponse = rawResponse;
    }
}
