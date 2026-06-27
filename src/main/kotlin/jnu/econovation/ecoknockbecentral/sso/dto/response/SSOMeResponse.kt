package jnu.econovation.ecoknockbecentral.sso.dto.response

data class SSOMeResponse(
    val memberId: Long,
    val name: String,
    val generation: Int,
    val status: String,
    val role: String,
)