package jnu.econovation.ecoknockbecentral.auth.service

import jnu.econovation.ecoknockbecentral.admin.config.AdminConfig
import jnu.econovation.ecoknockbecentral.auth.config.AuthPolicyConfig
import jnu.econovation.ecoknockbecentral.auth.exception.BadAdminMasterPasswordException
import jnu.econovation.ecoknockbecentral.auth.exception.GuestLoginRateLimitExceededException
import jnu.econovation.ecoknockbecentral.auth.repository.GuestLoginRateLimitRepository
import jnu.econovation.ecoknockbecentral.auth.repository.RefreshTokenRepository
import jnu.econovation.ecoknockbecentral.auth.repository.RefreshTokenRotationResult
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.vo.Role
import jnu.econovation.ecoknockbecentral.member.service.MemberService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.Duration
import java.time.Instant

class AuthServiceTest {
    private val jwtUtil = mock<JwtUtil>()
    private val memberService = mock<MemberService>()
    private val refreshTokenRepository = mock<RefreshTokenRepository>()
    private val guestLoginRateLimitRepository = mock<GuestLoginRateLimitRepository>()
    private val adminConfig = AdminConfig(masterPassword = "master-password", ssoMemberId = 1L)
    private val authPolicyConfig = AuthPolicyConfig(
        accessTokenTTL = Duration.ofHours(6),
        refreshTokenTTL = Duration.ofDays(60),
        guestSessionTTL = Duration.ofHours(24),
        guestLoginRateLimit = 5,
        guestLoginRateLimitWindow = Duration.ofHours(1),
    )

    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        authService = AuthService(
            jwtUtil = jwtUtil,
            memberService = memberService,
            refreshTokenRepository = refreshTokenRepository,
            guestLoginRateLimitRepository = guestLoginRateLimitRepository,
            authPolicyConfig = authPolicyConfig,
            adminConfig = adminConfig,
        )
    }

    @Test
    @DisplayName("게스트 로그인은 세션 쿠키용 토큰을 발급하고 24시간 이내 TTL로 refresh token을 저장한다")
    fun issuesGuestSessionTokens() {
        val guest = guestMember(expiresAt = Instant.now().plus(Duration.ofHours(24)))
        whenever(guestLoginRateLimitRepository.tryAcquire("127.0.0.1")).thenReturn(true)
        whenever(memberService.createGuest(any())).thenReturn(guest)
        whenever(jwtUtil.generateAccessToken(eq(guest), any())).thenReturn("access")
        whenever(jwtUtil.generateRefreshToken(eq(guest), any())).thenReturn("refresh")
        whenever(jwtUtil.extractTokenId("refresh")).thenReturn("refresh-id")

        val result = authService.issueGuestToken("127.0.0.1")

        assertThat(result.isSessionCookie).isTrue()
        assertThat(result.accessToken).isEqualTo("access")
        assertThat(result.refreshToken).isEqualTo("refresh")

        val ttlCaptor = argumentCaptor<Duration>()
        verify(refreshTokenRepository).save(eq(guest.id), eq("refresh-id"), ttlCaptor.capture())
        assertThat(ttlCaptor.firstValue).isPositive().isLessThanOrEqualTo(Duration.ofHours(24))
    }

    @Test
    @DisplayName("IP 제한을 초과하면 게스트 회원을 생성하지 않는다")
    fun rejectsGuestLoginWhenRateLimited() {
        whenever(guestLoginRateLimitRepository.tryAcquire("127.0.0.1")).thenReturn(false)

        assertThatThrownBy { authService.issueGuestToken("127.0.0.1") }
            .isInstanceOf(GuestLoginRateLimitExceededException::class.java)

        verify(memberService, never()).createGuest(any())
    }

    @Test
    @DisplayName("올바른 관리자 마스터 비밀번호는 설정된 ADMIN 회원의 일반 토큰을 발급한다")
    fun issuesAdminToken() {
        val admin = adminMember()
        whenever(memberService.getBySSOMemberId(1L)).thenReturn(admin)
        whenever(jwtUtil.generateAccessToken(admin)).thenReturn("access")
        whenever(jwtUtil.generateRefreshToken(admin)).thenReturn("refresh")
        whenever(jwtUtil.extractTokenId("refresh")).thenReturn("refresh-id")

        val result = authService.issueAdminToken("master-password")

        assertThat(result.accessToken).isEqualTo("access")
        assertThat(result.refreshToken).isEqualTo("refresh")
        verify(refreshTokenRepository).save(admin.id, "refresh-id", authPolicyConfig.refreshTokenTTL)
    }

    @Test
    @DisplayName("잘못된 관리자 마스터 비밀번호는 인증 오류를 반환한다")
    fun rejectsInvalidAdminMasterPassword() {
        assertThatThrownBy { authService.issueAdminToken("wrong-password") }
            .isInstanceOf(BadAdminMasterPasswordException::class.java)
    }

    @Test
    @DisplayName("설정된 관리자가 없거나 ADMIN 역할이 아니면 의미 오류를 반환한다")
    fun rejectsMissingOrNonAdminConfiguredMember() {
        whenever(memberService.getBySSOMemberId(1L)).thenReturn(null)

        assertThatThrownBy { authService.issueAdminToken("master-password") }
            .isInstanceOf(jnu.econovation.ecoknockbecentral.common.exception.client.BadDataMeaningException::class.java)

        whenever(memberService.getBySSOMemberId(1L)).thenReturn(userMember())

        assertThatThrownBy { authService.issueAdminToken("master-password") }
            .isInstanceOf(jnu.econovation.ecoknockbecentral.common.exception.client.BadDataMeaningException::class.java)
    }

    @Test
    @DisplayName("로그아웃은 유효한 refresh token의 현재 jti만 폐기한다")
    fun revokesCurrentRefreshTokenOnLogout() {
        whenever(jwtUtil.validateRefreshToken("refresh")).thenReturn(true)
        whenever(jwtUtil.extractId("refresh")).thenReturn(1L)
        whenever(jwtUtil.extractTokenId("refresh")).thenReturn("refresh-id")

        authService.logout("refresh")

        verify(refreshTokenRepository).deleteIfMatches(1L, "refresh-id")
    }

    @Test
    @DisplayName("로그아웃은 누락되거나 유효하지 않은 refresh token을 무시한다")
    fun ignoresMissingOrInvalidRefreshTokenOnLogout() {
        whenever(jwtUtil.validateRefreshToken("invalid")).thenReturn(false)

        authService.logout(null)
        authService.logout("invalid")

        verify(refreshTokenRepository, never()).deleteIfMatches(any(), any())
    }

    @Test
    @DisplayName("로그아웃은 refresh session 폐기 실패를 외부로 전파하지 않는다")
    fun suppressesRefreshSessionRevocationFailureOnLogout() {
        whenever(jwtUtil.validateRefreshToken("refresh")).thenReturn(true)
        whenever(jwtUtil.extractId("refresh")).thenReturn(1L)
        whenever(jwtUtil.extractTokenId("refresh")).thenReturn("refresh-id")
        whenever(refreshTokenRepository.deleteIfMatches(1L, "refresh-id"))
            .thenThrow(IllegalStateException("redis unavailable"))

        authService.logout("refresh")
    }

    @Test
    @DisplayName("게스트 재발급은 최초 만료 시각을 넘지 않는 세션 쿠키용 토큰을 발급한다")
    fun reissuesGuestTokenWithinOriginalExpiration() {
        val guest = guestMember(expiresAt = Instant.now().plus(Duration.ofMinutes(30)))
        whenever(jwtUtil.validateRefreshToken("old-refresh")).thenReturn(true)
        whenever(jwtUtil.extractId("old-refresh")).thenReturn(guest.id)
        whenever(jwtUtil.extractTokenId("old-refresh")).thenReturn("old-id")
        whenever(memberService.get(guest.id)).thenReturn(guest)
        whenever(jwtUtil.generateRefreshToken(eq(guest), any())).thenReturn("new-refresh")
        whenever(jwtUtil.extractTokenId("new-refresh")).thenReturn("new-id")
        whenever(
            refreshTokenRepository.replaceIfMatches(
                eq(guest.id),
                eq("old-id"),
                eq("new-id"),
                any(),
            )
        ).thenReturn(RefreshTokenRotationResult.SUCCESSFULLY_REPLACED)
        whenever(jwtUtil.generateAccessToken(eq(guest), any())).thenReturn("new-access")

        val result = authService.reissue("old-refresh")

        assertThat(result.isSessionCookie).isTrue()
        val ttlCaptor = argumentCaptor<Duration>()
        verify(refreshTokenRepository).replaceIfMatches(
            eq(guest.id),
            eq("old-id"),
            eq("new-id"),
            ttlCaptor.capture(),
        )
        assertThat(ttlCaptor.firstValue).isPositive().isLessThan(Duration.ofMinutes(30))
    }

    @Test
    @DisplayName("만료된 게스트는 refresh token과 함께 정리한다")
    fun cleansUpExpiredGuests() {
        whenever(memberService.getExpiredGuestIds(any())).thenReturn(listOf(1L, 2L))

        authService.cleanupExpiredGuests()

        verify(refreshTokenRepository).delete(1L)
        verify(refreshTokenRepository).delete(2L)
        verify(memberService).deleteExpiredGuest(eq(1L), any())
        verify(memberService).deleteExpiredGuest(eq(2L), any())
    }

    private fun guestMember(expiresAt: Instant): MemberInfoDTO {
        return MemberInfoDTO(
            id = 1L,
            ssoMemberId = null,
            role = Role.GUEST,
            cohort = null,
            name = "게스트",
            status = null,
            guestExpiresAt = expiresAt,
        )
    }

    private fun adminMember() = userMember(role = Role.ADMIN)

    private fun userMember(role: Role = Role.USER) = MemberInfoDTO(
        id = 1L,
        ssoMemberId = 1L,
        role = role,
        cohort = null,
        name = "관리자",
        status = null,
        guestExpiresAt = null,
    )
}
