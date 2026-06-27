package jnu.econovation.ecoknockbecentral.auth.service

import jnu.econovation.ecoknockbecentral.auth.dto.AuthTokenDTO
import jnu.econovation.ecoknockbecentral.auth.exception.BadRefreshTokenException
import jnu.econovation.ecoknockbecentral.auth.repository.RefreshTokenRepository
import jnu.econovation.ecoknockbecentral.auth.repository.RefreshTokenRotationResult.*
import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil
import jnu.econovation.ecoknockbecentral.member.service.MemberService
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jwtUtil: JwtUtil,
    private val memberService: MemberService,
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun reissue(refreshToken: String?): AuthTokenDTO {
        if (refreshToken.isNullOrBlank()) {
            throw BadRefreshTokenException()
        }

        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw BadRefreshTokenException()
        }

        val memberId = jwtUtil.extractId(refreshToken) ?: throw BadRefreshTokenException()
        val tokenId = jwtUtil.extractTokenId(refreshToken) ?: throw BadRefreshTokenException()

        val memberInfo = memberService.get(memberId)
            ?: throw InternalServerException(IllegalStateException("리프레시 토큰은 이상 없으나 id가 ${memberId}인 회원을 찾지 못함"))

        val newRefreshToken = jwtUtil.generateRefreshToken(memberInfo)

        val newRefreshTokenId = jwtUtil.extractTokenId(newRefreshToken)
            ?: throw InternalServerException(IllegalStateException("발급한 refresh token에서 jti 추출 실패"))

        when (
            refreshTokenRepository.replaceIfMatches(
                memberId = memberId,
                currentTokenId = tokenId,
                nextTokenId = newRefreshTokenId
            )
        ) {
            SUCCESSFULLY_REPLACED -> Unit
            MISSING -> {
                logger.warn { "refresh token 저장 정보 없음 -> memberId: $memberId, tokenId: $tokenId" }
                throw BadRefreshTokenException()
            }

            MISMATCHED -> {
                logger.warn { "refresh token id가 mismatched. 재사용 or 탈취 의심 -> memberId: $memberId, tokenId: $tokenId" }
                throw BadRefreshTokenException()
            }

            FAILED -> throw InternalServerException(IllegalStateException("refresh token 회전 결과를 해석할 수 없음"))
        }

        return AuthTokenDTO(
            accessToken = jwtUtil.generateAccessToken(memberInfo),
            refreshToken = newRefreshToken,
        )
    }
}
