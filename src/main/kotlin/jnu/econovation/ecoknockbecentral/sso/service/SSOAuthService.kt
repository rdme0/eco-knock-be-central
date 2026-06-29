package jnu.econovation.ecoknockbecentral.sso.service

import jnu.econovation.ecoknockbecentral.auth.repository.RefreshTokenRepository
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil
import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import jnu.econovation.ecoknockbecentral.member.service.MemberService
import jnu.econovation.ecoknockbecentral.sso.client.SSOAuthClient
import jnu.econovation.ecoknockbecentral.sso.dto.SSOAuthResultDTO
import jnu.econovation.ecoknockbecentral.sso.dto.SSOMeDTO
import jnu.econovation.ecoknockbecentral.sso.dto.response.SSOMeResponse
import jnu.econovation.ecoknockbecentral.sso.exception.BadSSOTokenException
import org.springframework.stereotype.Service

@Service
class SSOAuthService(
    private val ssoAuthClient: SSOAuthClient,
    private val memberService: MemberService,
    private val jwtUtil: JwtUtil,
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    fun authenticateMember(
        ssoAccessToken: String?
    ): SSOAuthResultDTO {
        if (ssoAccessToken.isNullOrBlank()) {
            throw BadSSOTokenException()
        }

        val response: SSOMeResponse = ssoAuthClient.getMe(ssoAccessToken)
        val memberInfo = memberService.getOrSaveFromSso(dto = SSOMeDTO.from(response))
        val accessToken = jwtUtil.generateAccessToken(memberInfo)
        val refreshToken = jwtUtil.generateRefreshToken(memberInfo)
        val refreshTokenId = jwtUtil.extractTokenId(refreshToken)
            ?: throw InternalServerException(
                IllegalStateException("발급한 refresh token에서 jti 추출 실패")
            )

        refreshTokenRepository.save(memberInfo.id, refreshTokenId)

        return SSOAuthResultDTO(
            memberInfo = memberInfo,
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}
