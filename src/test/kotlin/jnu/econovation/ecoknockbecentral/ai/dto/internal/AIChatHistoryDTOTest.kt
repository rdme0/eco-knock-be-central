package jnu.econovation.ecoknockbecentral.ai.dto.internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AIChatHistoryDTOTest {
    @Test
    fun `질문과 답변을 역할 라벨 형식으로 표현한다`() {
        val history = AIChatHistoryDTO("이전 질문", "이전 답변")

        assertThat(history.toString()).isEqualTo("사용자: 이전 질문\nAI: 이전 답변")
    }
}
