package jnu.econovation.ecoknockbecentral.sso.controller

import jnu.econovation.ecoknockbecentral.auth.config.AuthPolicyConfig
import jnu.econovation.ecoknockbecentral.auth.core.passport.Passport
import jnu.econovation.ecoknockbecentral.auth.web.resolver.PassportArgumentResolver
import jnu.econovation.ecoknockbecentral.common.exception.handler.GlobalExceptionHandler
import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO
import jnu.econovation.ecoknockbecentral.member.model.vo.Role
import jnu.econovation.ecoknockbecentral.sso.config.SSOConfig
import jnu.econovation.ecoknockbecentral.sso.constant.SSOConstant.SSO_REDIRECT_URL_KEY
import jnu.econovation.ecoknockbecentral.sso.dto.SSOAuthResultDTO
import jnu.econovation.ecoknockbecentral.sso.exception.BadSSOTokenException
import jnu.econovation.ecoknockbecentral.sso.resolver.SSORedirectUrlResolver
import jnu.econovation.ecoknockbecentral.sso.service.SSOAuthService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockCookie
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.databind.json.JsonMapper
import java.time.Duration
import java.time.LocalDateTime
import java.util.Base64

class SSOAuthControllerTest {
    private val service = mock<SSOAuthService>()
    private val redirectUrlResolver = mock<SSORedirectUrlResolver>()
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val controller = SSOAuthController(
            service = service,
            config = SSOConfig(
                loginPageBaseUrl = "https://auth.example.com",
                gatewayPassportUrl = "https://api.example.com/api/eco-knock",
                clientId = "client-id",
            ),
            redirectUrlResolver = redirectUrlResolver,
            authPolicyConfig = AuthPolicyConfig(
                accessTokenTTL = Duration.ofHours(6),
                refreshTokenTTL = Duration.ofDays(60),
                guestSessionTTL = Duration.ofHours(24),
                guestLoginRateLimit = 5,
                guestLoginRateLimitWindow = Duration.ofHours(1),
            ),
        )

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(
                PassportArgumentResolver(JsonMapper.builder().findAndAddModules().build())
            )
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }

    @Test
    fun `SSO 로그인은 APP client type으로 이동한다`() {
        mockMvc.perform(get("/sso/login"))
            .andExpect(status().isFound)
            .andExpect(header().string(HttpHeaders.LOCATION, "https://auth.example.com?client-id=client-id&client-type=app"))
    }

    @Test
    fun `APP callback은 access token으로 서비스 쿠키를 발급하고 원래 주소로 이동한다`() {
        whenever(redirectUrlResolver.resolve("http://localhost:3000/admin"))
            .thenReturn("http://localhost:3000/admin")
        whenever(service.authenticateMember("sso-access-token"))
            .thenReturn(authResult())

        mockMvc.perform(
            get("/sso/callback")
                .param("accessToken", "sso-access-token")
                .cookie(MockCookie(SSO_REDIRECT_URL_KEY, "http://localhost:3000/admin")),
        )
            .andExpect(status().isFound)
            .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost:3000/admin"))
            .andExpect(header().string("Referrer-Policy", "no-referrer"))
            .andExpect(cookie().value("accessToken", "access-token"))
            .andExpect(cookie().value("refreshToken", "refresh-token"))
            .andExpect(cookie().maxAge(SSO_REDIRECT_URL_KEY, 0))

        verify(service).authenticateMember("sso-access-token")
    }

    @Test
    fun `APP callback은 access token이 없으면 401을 반환하고 서비스 쿠키를 발급하지 않는다`() {
        invalidCallback(null)

        mockMvc.perform(
            get("/sso/callback")
                .cookie(MockCookie(SSO_REDIRECT_URL_KEY, "http://localhost:3000/admin")),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(cookie().doesNotExist("accessToken"))
            .andExpect(cookie().doesNotExist("refreshToken"))

        verify(service).authenticateMember(null)
    }

    @Test
    fun `APP callback은 유효하지 않은 access token이면 401을 반환한다`() {
        invalidCallback("invalid-token")

        mockMvc.perform(
            get("/sso/callback")
                .param("accessToken", "invalid-token")
                .cookie(MockCookie(SSO_REDIRECT_URL_KEY, "http://localhost:3000/admin")),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(cookie().doesNotExist("accessToken"))
            .andExpect(cookie().doesNotExist("refreshToken"))

        verify(service).authenticateMember("invalid-token")
    }

    @Test
    fun `Gateway Passport endpoint는 encoded Passport를 읽어 회원 정보를 반환한다`() {
        val passport = """{
            "memberId": 1,
            "loginId": "private-login-id",
            "name": "회원",
            "generation": 16,
            "status": "AM",
            "roles": ["USER"],
            "issuedAt": "2026-07-20T12:00:00",
            "expiresAt": "2099-07-20T12:00:00"
        }""".trimIndent()
        val encodedPassport = Base64.getEncoder().encodeToString(passport.toByteArray())

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/sso/passport")
                .header("X-User-Passport", encodedPassport),
        )
            .andExpect(status().isOk)
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.memberId").value(1))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.roles[0]").value("USER"))
    }

    @Test
    fun `Gateway Passport endpoint는 Passport 헤더가 없으면 401을 반환한다`() {
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/sso/passport"),
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `Gateway Passport endpoint는 손상된 Passport 헤더면 400을 반환한다`() {
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/sso/passport")
                .header("X-User-Passport", "not-base64"),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `Gateway Passport endpoint는 만료된 Passport 헤더면 401을 반환한다`() {
        val passport = Passport(
            1L,
            "private-login-id",
            "회원",
            16,
            "AM",
            listOf("USER"),
            LocalDateTime.now().minusHours(2),
            LocalDateTime.now().minusHours(1),
        )
        val passportJson = """{
            "memberId": ${passport.memberId},
            "name": "${passport.name}",
            "generation": ${passport.generation},
            "status": "${passport.status}",
            "roles": ["USER"],
            "issuedAt": "${passport.issuedAt}",
            "expiresAt": "${passport.expiresAt}"
        }""".trimIndent()

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/sso/passport")
                .header("X-User-Passport", Base64.getEncoder().encodeToString(passportJson.toByteArray())),
        )
            .andExpect(status().isUnauthorized)
    }

    private fun invalidCallback(accessToken: String?) {
        whenever(redirectUrlResolver.resolve(anyOrNull()))
            .thenReturn("http://localhost:3000/admin")
        doThrow(BadSSOTokenException())
            .whenever(service)
            .authenticateMember(accessToken)
    }

    private fun authResult() = SSOAuthResultDTO(
        memberInfo = MemberInfoDTO(
            id = 1L,
            ssoMemberId = 2L,
            role = Role.USER,
            cohort = null,
            name = "회원",
            status = null,
        ),
        accessToken = "access-token",
        refreshToken = "refresh-token",
    )
}
