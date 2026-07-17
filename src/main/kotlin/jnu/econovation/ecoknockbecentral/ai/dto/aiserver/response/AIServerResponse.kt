package jnu.econovation.ecoknockbecentral.ai.dto.aiserver.response

import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AIServerResponse (
    val answer: String,
    val sources: List<String>,
    val usedRetrieval: Boolean
)