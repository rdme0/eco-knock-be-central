package jnu.econovation.ecoknockbecentral.ai.service

import jnu.econovation.ecoknockbecentral.ai.client.AIServerClient
import jnu.econovation.ecoknockbecentral.ai.dto.aiserver.RawAIServerResponseDTO
import jnu.econovation.ecoknockbecentral.ai.dto.aiserver.response.AIServerResponse
import jnu.econovation.ecoknockbecentral.ai.dto.client.request.AIChatRequest
import jnu.econovation.ecoknockbecentral.ai.dto.internal.AIChatHistoryDTO
import jnu.econovation.ecoknockbecentral.ai.service.internal.AIChatHistoryService
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class AIService(
    private val client: AIServerClient,
    private val historyService: AIChatHistoryService
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun chat(memberInfo: MemberInfoDTO, request: AIChatRequest): AIServerResponse {
        val history = historyService.getChatHistory(memberInfo)
        val questionWithHistory = buildQuestionWithHistory(history, request.question)
        val response = client.chat(question = questionWithHistory)

        runCatching {
            historyService.saveChat(
                memberInfo = memberInfo,
                question = request.question,
                answer = response.answer,
                rawResponse = RawAIServerResponseDTO.from(response)
            )
        }.onFailure {
            logger.error(it) { "AI chat history 저장 실패: memberId=${memberInfo.id}" }
        }

        return response
    }

    private fun buildQuestionWithHistory(
        history: List<AIChatHistoryDTO>,
        currentQuestion: String,
    ): String {
        return buildString {
            appendLine("이전 대화:")
            if (history.isNotEmpty()) {
                append(history.joinToString("\n\n"))
                appendLine()
                appendLine()
            }
            appendLine("현재 질문:")
            append("사용자: ")
            append(currentQuestion)
        }
    }
}
