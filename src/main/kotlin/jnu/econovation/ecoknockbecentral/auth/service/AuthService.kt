package jnu.econovation.ecoknockbecentral.auth.service

import jnu.econovation.ecoknockbecentral.auth.config.TestAuthConfig
import jnu.econovation.ecoknockbecentral.auth.config.AuthPolicyConfig
import jnu.econovation.ecoknockbecentral.auth.dto.AuthTokenDTO
import jnu.econovation.ecoknockbecentral.auth.exception.BadRefreshTokenException
import jnu.econovation.ecoknockbecentral.auth.exception.BadTestTokenPasswordException
import jnu.econovation.ecoknockbecentral.auth.exception.GuestLoginRateLimitExceededException
import jnu.econovation.ecoknockbecentral.auth.repository.GuestLoginRateLimitRepository
import jnu.econovation.ecoknockbecentral.auth.repository.RefreshTokenRepository
import jnu.econovation.ecoknockbecentral.auth.repository.RefreshTokenRotationResult.*
import jnu.econovation.ecoknockbecentral.common.extension.isEqualConstantTime
import jnu.econovation.ecoknockbecentral.common.exception.client.BadDataMeaningException
import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import jnu.econovation.ecoknockbecentral.common.security.config.AdminSecurityConfig
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil
import jnu.econovation.ecoknockbecentral.member.service.MemberService
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.vo.Role
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class AuthService(
    private val jwtUtil: JwtUtil,
    private val memberService: MemberService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val guestLoginRateLimitRepository: GuestLoginRateLimitRepository,
    private val authPolicyConfig: AuthPolicyConfig,
    private val adminSecurityConfig: AdminSecurityConfig,
    private val testAuthConfig: TestAuthConfig,
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

        val memberInfo = memberService.get(memberId) ?: throw BadRefreshTokenException()
        val isGuest = memberInfo.role == Role.GUEST
        val guestRemainingTTL = if (isGuest) guestRemainingTTL(memberInfo) else null

        val newRefreshToken = if (isGuest) {
            jwtUtil.generateRefreshToken(memberInfo, guestRemainingTTL)
        } else {
            jwtUtil.generateRefreshToken(memberInfo)
        }

        val newRefreshTokenId = jwtUtil.extractTokenId(newRefreshToken)
            ?: throw InternalServerException(IllegalStateException("발급한 refresh token에서 jti 추출 실패"))

        when (
            refreshTokenRepository.replaceIfMatches(
                memberId = memberId,
                currentTokenId = tokenId,
                nextTokenId = newRefreshTokenId,
                ttl = guestRemainingTTL ?: authPolicyConfig.refreshTokenTTL,
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
            accessToken = if (isGuest) {
                jwtUtil.generateAccessToken(memberInfo, guestRemainingTTL)
            } else {
                jwtUtil.generateAccessToken(memberInfo)
            },
            refreshToken = newRefreshToken,
            isSessionCookie = isGuest,
        )
    }

    fun issueGuestToken(clientIp: String): AuthTokenDTO {
        if (!guestLoginRateLimitRepository.tryAcquire(clientIp)) {
            throw GuestLoginRateLimitExceededException()
        }

        val now = Instant.now()
        val memberInfo = memberService.createGuest(now.plus(authPolicyConfig.guestSessionTTL))
        val ttl = guestRemainingTTL(memberInfo)
        val refreshToken = jwtUtil.generateRefreshToken(memberInfo, ttl)
        val refreshTokenId = jwtUtil.extractTokenId(refreshToken)
            ?: throw InternalServerException(IllegalStateException("발급한 refresh token에서 jti 추출 실패"))

        refreshTokenRepository.save(memberInfo.id, refreshTokenId, ttl)

        return AuthTokenDTO(
            accessToken = jwtUtil.generateAccessToken(memberInfo, ttl),
            refreshToken = refreshToken,
            isSessionCookie = true,
        )
    }

    fun issueTestToken(password: String?): AuthTokenDTO {
        if (!adminSecurityConfig.masterPassword.isEqualConstantTime(password)) {
            throw BadTestTokenPasswordException()
        }

        val ssoMemberId = testAuthConfig.ssoMemberId

        val memberInfo = memberService.getBySSOMemberId(ssoMemberId)
            ?: throw BadDataMeaningException("테스트 인증 대상 회원을 찾을 수 없습니다.")

        val refreshToken = jwtUtil.generateRefreshToken(memberInfo)
        val refreshTokenId = jwtUtil.extractTokenId(refreshToken)
            ?: throw InternalServerException(IllegalStateException("발급한 refresh token에서 jti 추출 실패"))

        refreshTokenRepository.save(memberInfo.id, refreshTokenId)

        return AuthTokenDTO(
            accessToken = jwtUtil.generateAccessToken(memberInfo),
            refreshToken = refreshToken,
        )
    }

    fun cleanupExpiredGuests() {
        val now = Instant.now()

        memberService.getExpiredGuestIds(now).forEach { memberId ->
            refreshTokenRepository.delete(memberId)
            memberService.deleteExpiredGuest(memberId, now)
        }
    }

    private fun guestRemainingTTL(memberInfo: MemberInfoDTO): Duration {
        val guestExpiresAt = memberInfo.guestExpiresAt ?: throw BadRefreshTokenException()
        val ttl = Duration.between(Instant.now(), guestExpiresAt)

        if (ttl.isZero || ttl.isNegative) {
            throw BadRefreshTokenException()
        }

        return ttl
    }
}
