package jnu.econovation.ecoknockbecentral.sso.dto.response

import jnu.econovation.ecoknockbecentral.auth.core.passport.Passport

data class SSOPassportResponse(
    val memberId: Long,
    val name: String,
    val generation: Int,
    val status: String,
    val roles: List<String>,
) {
    companion object {
        fun from(passport: Passport): SSOPassportResponse {
            return SSOPassportResponse(
                memberId = passport.memberId,
                name = passport.name,
                generation = passport.generation,
                status = passport.status,
                roles = passport.roles,
            )
        }
    }
}
