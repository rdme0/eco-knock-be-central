package jnu.econovation.ecoknockbecentral.auth.dto.request

import io.swagger.v3.oas.annotations.media.Schema

data class TestTokenRequest(
    @field:Schema(description = "관리자 마스터 비밀번호", example = "master-password")
    val password: String?,
)
