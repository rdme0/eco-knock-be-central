package jnu.econovation.ecoknockbecentral.whozin.dto.response

import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming
import java.time.LocalDateTime
import java.util.*

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class WhozinMembersResponse(
    val generatedAt: LocalDateTime,
    val data: WhozinData,
    val message: String,
)

data class WhozinData(
    val dates: List<WhozinMembersByDate>,
)

data class WhozinMembersByDate(
    val date: List<Int>,
    val members: List<WhozinMember>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class WhozinMember(
    val memberId: UUID,
    val generation: Int,
    val memberName: String,
    val presenceDuration: String,
)
