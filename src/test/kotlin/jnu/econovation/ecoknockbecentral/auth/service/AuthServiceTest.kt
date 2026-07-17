package jnu.econovation.ecoknockbecentral.auth.service

import jnu.econovation.ecoknockbecentral.auth.config.AuthPolicyConfig
import jnu.econovation.ecoknockbecentral.auth.config.TestAuthConfig
import jnu.econovation.ecoknockbecentral.auth.exception.GuestLoginRateLimitExceededException
import jnu.econovation.ecoknockbecentral.auth.repository.GuestLoginRateLimitRepository
import jnu.econovation.ecoknockbecentral.auth.repository.RefreshTokenRepository
import jnu.econovation.ecoknockbecentral.auth.repository.RefreshTokenRotationResult
import jnu.econovation.ecoknockbecentral.common.security.config.AdminSecurityConfig
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.vo.Role
import jnu.econovation.ecoknockbecentral.member.service.MemberService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Duration
import java.time.Instant

class AuthServiceTest {
    private val jwtUtil = mock<JwtUtil>()
    private val memberService = mock<MemberService>()
    private val refreshTokenRepository = mock<RefreshTokenRepository>()
    private val guestLoginRateLimitRepository = mock<GuestLoginRateLimitRepository>()
    private val adminSecurityConfig = mock<AdminSecurityConfig>()
    private val testAuthConfig = TestAuthConfig(ssoMemberId = 0)
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
            adminSecurityConfig = adminSecurityConfig,
            testAuthConfig = testAuthConfig,
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
}
