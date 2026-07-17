package jnu.econovation.ecoknockbecentral.ai.dto.internal

import jnu.econovation.ecoknockbecentral.ai.model.entity.AIChatHistory

data class AIChatHistoryDTO(
    val question: String,
    val answer: String,
) {
    override fun toString(): String {
        return "사용자: $question\nAI: $answer"
    }

    companion object {
        fun from(history: AIChatHistory): AIChatHistoryDTO {
            return AIChatHistoryDTO(
                question = history.question,
                answer = history.answer,
            )
        }
    }
}
