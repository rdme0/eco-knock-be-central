package jnu.econovation.ecoknockbecentral.ai.dto.aiserver

import jnu.econovation.ecoknockbecentral.ai.dto.aiserver.response.AIServerResponse

data class RawAIServerResponseDTO (
    val answer: String,
    val sources: List<String>,
    val usedRAG: Boolean
) {
    companion object {
        fun from(response: AIServerResponse): RawAIServerResponseDTO {
            return RawAIServerResponseDTO(
                answer = response.answer,
                sources = response.sources,
                usedRAG = response.usedRetrieval
            )
        }
    }
}