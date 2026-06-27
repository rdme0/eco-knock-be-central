package jnu.econovation.ecoknockbecentral.sso.dto

import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO

data class SSOAuthResultDTO(
    val memberInfo: MemberInfoDTO,
    val accessToken: String,
    val refreshToken: String,
)
