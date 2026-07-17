package jnu.econovation.ecoknockbecentral.auth.dto

data class AuthTokenDTO(
    val accessToken: String,
    val refreshToken: String,
    val isSessionCookie: Boolean = false,
)
