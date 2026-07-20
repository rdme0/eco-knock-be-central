package jnu.econovation.ecoknockbecentral.sso.controller

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jnu.econovation.ecoknockbecentral.auth.config.AuthPolicyConfig
import jnu.econovation.ecoknockbecentral.auth.constant.AuthConstant.ACCESS_TOKEN
import jnu.econovation.ecoknockbecentral.auth.constant.AuthConstant.REFRESH_TOKEN
import jnu.econovation.ecoknockbecentral.auth.core.passport.Passport
import jnu.econovation.ecoknockbecentral.auth.web.annotation.PassportAuth
import jnu.econovation.ecoknockbecentral.common.cookie.util.CookieUtil
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_SSO_TOKEN_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_SSO_TOKEN_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.INVALID_REDIRECT_URI_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.INVALID_REDIRECT_URI_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.REDIRECT_RESPONSE
import jnu.econovation.ecoknockbecentral.sso.config.SSOConfig
import jnu.econovation.ecoknockbecentral.sso.constant.SSOConstant.CLIENT_TYPE_APP
import jnu.econovation.ecoknockbecentral.sso.constant.SSOConstant.SSO_REDIRECT_URL_KEY
import jnu.econovation.ecoknockbecentral.sso.dto.SSOAuthResultDTO
import jnu.econovation.ecoknockbecentral.sso.dto.response.SSOPassportResponse
import jnu.econovation.ecoknockbecentral.sso.resolver.SSORedirectUrlResolver
import jnu.econovation.ecoknockbecentral.sso.service.SSOAuthService
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.WebUtils

@RestController
@RequestMapping("/sso")
@Tag(name = "SSO", description = "Econovation SSO 로그인 API")
class SSOAuthController(
    private val service: SSOAuthService,
    private val config: SSOConfig,
    private val redirectUrlResolver: SSORedirectUrlResolver,
    private val authPolicyConfig: AuthPolicyConfig
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @GetMapping("/login")
    @Operation(
        summary = "SSO 로그인 시작",
        description = "redirect 파라미터를 쿠키로 저장한 뒤 Econovation SSO 로그인 페이지로 이동합니다.",
        parameters = [
            Parameter(
                name = "redirect",
                `in` = ParameterIn.QUERY,
                required = true,
                description = "SSO 완료 후 이동할 프론트 URL",
                schema = Schema(type = "string", format = "uri"),
                example = "https://frontend.example.com"
            )
        ],
        security = [],
        responses = [
            ApiResponse(responseCode = "302", description = REDIRECT_RESPONSE, content = [Content()]),
            ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 redirect URI",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        name = INVALID_REDIRECT_URI_EXAMPLE_NAME,
                        ref = INVALID_REDIRECT_URI_EXAMPLE_REF
                    )]
                )]
            )
        ]
    )
    fun login(response: HttpServletResponse) {
        val ssoRedirectUrl = UriComponentsBuilder.fromUriString(config.loginPageBaseUrl)
            .queryParam("client-id", config.clientId)
            .queryParam("client-type", CLIENT_TYPE_APP)
            .build()
            .toUriString()

        response.sendRedirect(ssoRedirectUrl)
    }

    @GetMapping("/callback")
    @Operation(
        summary = "SSO 콜백 처리",
        description = "APP 방식 SSO callback의 accessToken 쿼리로 회원을 인증하고 서비스 인증 쿠키를 발급한 뒤 프론트 URL로 이동합니다.",
        parameters = [
            Parameter(
                name = "accessToken",
                `in` = ParameterIn.QUERY,
                required = true,
                description = "APP 방식 SSO callback access token",
                schema = Schema(type = "string"),
            )
        ],
        security = [],
        responses = [
            ApiResponse(responseCode = "302", description = REDIRECT_RESPONSE, content = [Content()]),
            ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 redirect URI",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        name = INVALID_REDIRECT_URI_EXAMPLE_NAME,
                        ref = INVALID_REDIRECT_URI_EXAMPLE_REF
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 SSO 토큰",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(name = BAD_SSO_TOKEN_EXAMPLE_NAME, ref = BAD_SSO_TOKEN_EXAMPLE_REF)]
                )]
            )
        ]
    )
    fun callback(
        @RequestParam(name = "accessToken", required = false)
        ssoAccessToken: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val frontendRedirectUrl = redirectUrlResolver.resolve(
            WebUtils.getCookie(request, SSO_REDIRECT_URL_KEY)?.value
        )

        val result: SSOAuthResultDTO = service.authenticateMember(ssoAccessToken)

        CookieUtil.addCookie(
            request,
            response,
            ACCESS_TOKEN,
            result.accessToken,
            authPolicyConfig.accessTokenTTL.toSeconds().toInt(),
        )

        CookieUtil.addCookie(
            request,
            response,
            REFRESH_TOKEN,
            result.refreshToken,
            authPolicyConfig.refreshTokenTTL.toSeconds().toInt(),
        )

        CookieUtil.removeCookie(request, response, SSO_REDIRECT_URL_KEY)

        response.setHeader("Referrer-Policy", "no-referrer")
        response.sendRedirect(frontendRedirectUrl)
    }

    @Hidden
    @PostMapping("/passport", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun passport(
        @PassportAuth(validateExpiry = true) passport: Passport,
    ): SSOPassportResponse {
        logger.info { "Gateway Passport 수신: memberId=${passport.memberId}, roles=${passport.roles}" }
        return SSOPassportResponse.from(passport)
    }
}
