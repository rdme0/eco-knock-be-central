package jnu.econovation.ecoknockbecentral.ai.service

import jnu.econovation.ecoknockbecentral.ai.client.AIServerClient
import jnu.econovation.ecoknockbecentral.ai.dto.aiserver.RawAIServerResponseDTO
import jnu.econovation.ecoknockbecentral.ai.dto.aiserver.response.AIServerResponse
import jnu.econovation.ecoknockbecentral.ai.dto.client.request.AIChatRequest
import jnu.econovation.ecoknockbecentral.ai.dto.internal.AIChatHistoryDTO
import jnu.econovation.ecoknockbecentral.ai.service.internal.AIChatHistoryService
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.vo.ActiveStatus
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort
import jnu.econovation.ecoknockbecentral.member.model.vo.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import org.mockito.Mockito.`when`

class AIServiceTest {
    private val client = mock(AIServerClient::class.java)
    private val historyService = mock(AIChatHistoryService::class.java)
    private val service = AIService(client, historyService)
    private val memberInfo = MemberInfoDTO(
        1L,
        100L,
        Role.USER,
        Cohort(1),
        "테스트 회원",
        ActiveStatus.OB,
    )
    private val response = AIServerResponse(
        answer = "답변",
        sources = listOf("https://example.com"),
        usedRetrieval = true,
    )

    @Test
    fun `최근 대화 20쌍을 오래된 순서의 대화록으로 AI 서버에 전달한다`() {
        val history = (20 downTo 1).map { AIChatHistoryDTO("질문$it", "답변$it") }
        `when`(historyService.getChatHistory(memberInfo)).thenReturn(history)
        `when`(client.chat(org.mockito.ArgumentMatchers.anyString())).thenReturn(response)

        val result = service.chat(memberInfo, AIChatRequest("현재 질문"))

        assertThat(result).isEqualTo(response)
        assertThat(sentQuestion())
            .contains("사용자: 질문20\nAI: 답변20")
            .contains("사용자: 질문1\nAI: 답변1")
            .contains("현재 질문:\n사용자: 현재 질문")
        assertThat(Regex("사용자:").findAll(sentQuestion()).count()).isEqualTo(21)
    }

    @Test
    fun `이력이 없어도 현재 질문만 전달한다`() {
        `when`(historyService.getChatHistory(memberInfo)).thenReturn(emptyList())
        `when`(client.chat(org.mockito.ArgumentMatchers.anyString())).thenReturn(response)

        service.chat(memberInfo, AIChatRequest("현재 질문"))

        assertThat(sentQuestion()).isEqualTo("이전 대화:\n현재 질문:\n사용자: 현재 질문")
    }

    @Test
    fun `이력 저장 실패에도 AI 응답을 반환한다`() {
        `when`(historyService.getChatHistory(memberInfo)).thenReturn(emptyList())
        `when`(client.chat(org.mockito.ArgumentMatchers.anyString())).thenReturn(response)
        doThrow(IllegalStateException("DB unavailable"))
            .`when`(historyService)
            .saveChat(memberInfo, "현재 질문", response.answer, RawAIServerResponseDTO.from(response))

        val result = service.chat(memberInfo, AIChatRequest("현재 질문"))

        assertThat(result).isEqualTo(response)
    }

    private fun sentQuestion(): String {
        return mockingDetails(client).invocations.single { it.method.name == "chat" }.arguments.single() as String
    }
}
