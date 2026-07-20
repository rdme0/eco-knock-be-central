package jnu.econovation.ecoknockbecentral.auth.service

import jnu.econovation.ecoknockbecentral.admin.config.AdminConfig
import jnu.econovation.ecoknockbecentral.auth.config.AuthPolicyConfig
import jnu.econovation.ecoknockbecentral.auth.dto.AuthTokenDTO
import jnu.econovation.ecoknockbecentral.auth.exception.BadAdminMasterPasswordException
import jnu.econovation.ecoknockbecentral.auth.exception.BadRefreshTokenException
import jnu.econovation.ecoknockbecentral.auth.exception.GuestLoginRateLimitExceededException
import jnu.econovation.ecoknockbecentral.auth.repository.GuestLoginRateLimitRepository
import jnu.econovation.ecoknockbecentral.auth.repository.RefreshTokenRepository
import jnu.econovation.ecoknockbecentral.auth.repository.RefreshTokenRotationResult.*
import jnu.econovation.ecoknockbecentral.common.exception.client.BadDataMeaningException
import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import jnu.econovation.ecoknockbecentral.common.extension.isEqualConstantTime
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.vo.Role
import jnu.econovation.ecoknockbecentral.member.service.MemberService
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
    private val adminConfig: AdminConfig,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val SYSTEM_ADMIN_MEMBER_ID = 0L
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

    fun issueAdminToken(password: String?): AuthTokenDTO {
        if (!adminConfig.masterPassword.isEqualConstantTime(password)) {
            throw BadAdminMasterPasswordException()
        }

        val memberInfo = memberService.get(SYSTEM_ADMIN_MEMBER_ID)
            ?: throw BadDataMeaningException("관리자 인증 대상 회원을 찾을 수 없습니다.")

        if (memberInfo.role != Role.ADMIN) {
            throw BadDataMeaningException("관리자 인증 대상 회원의 역할이 ADMIN이 아닙니다.")
        }

        val refreshToken = jwtUtil.generateRefreshToken(memberInfo)
        val refreshTokenId = jwtUtil.extractTokenId(refreshToken)
            ?: throw InternalServerException(IllegalStateException("발급한 refresh token에서 jti 추출 실패"))

        refreshTokenRepository.save(memberInfo.id, refreshTokenId, authPolicyConfig.refreshTokenTTL)

        return AuthTokenDTO(
            accessToken = jwtUtil.generateAccessToken(memberInfo),
            refreshToken = refreshToken,
        )
    }

    fun logout(refreshToken: String?) {
        if (refreshToken.isNullOrBlank() || !jwtUtil.validateRefreshToken(refreshToken)) {
            return
        }

        val memberId = jwtUtil.extractId(refreshToken) ?: return
        val tokenId = jwtUtil.extractTokenId(refreshToken) ?: return

        runCatching {
            refreshTokenRepository.deleteIfMatches(memberId, tokenId)
        }.onFailure { exception ->
            logger.warn(exception) { "refresh token 로그아웃 폐기 실패 -> memberId: $memberId, tokenId: $tokenId" }
        }
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
