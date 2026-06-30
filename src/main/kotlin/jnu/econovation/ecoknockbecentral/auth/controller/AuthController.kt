package jnu.econovation.ecoknockbecentral.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jnu.econovation.ecoknockbecentral.auth.config.AuthPolicyConfig
import jnu.econovation.ecoknockbecentral.auth.constant.AuthConstant.ACCESS_TOKEN
import jnu.econovation.ecoknockbecentral.auth.constant.AuthConstant.REFRESH_TOKEN
import jnu.econovation.ecoknockbecentral.auth.exception.BadRefreshTokenException
import jnu.econovation.ecoknockbecentral.auth.service.AuthService
import jnu.econovation.ecoknockbecentral.common.cookie.util.CookieUtil
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.emptySuccess
import jnu.econovation.ecoknockbecentral.common.openapi.OpenApiConstants.BAD_REFRESH_TOKEN_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.OpenApiConstants.BAD_REFRESH_TOKEN_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.OpenApiConstants.EMPTY_SUCCESS_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.OpenApiConstants.EMPTY_SUCCESS_EXAMPLE_REF
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth/reissue")
@Tag(name = "Auth", description = "인증 토큰 API")
class AuthController(
    private val authService: AuthService,
    private val authPolicyConfig: AuthPolicyConfig,
) {
    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "인증 토큰 재발급",
        description = "refreshToken 쿠키를 검증하고 accessToken, refreshToken 쿠키를 재발급합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "토큰 재발급 성공",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(
                        name = EMPTY_SUCCESS_EXAMPLE_NAME,
                        ref = EMPTY_SUCCESS_EXAMPLE_REF
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 refreshToken",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        name = BAD_REFRESH_TOKEN_EXAMPLE_NAME,
                        ref = BAD_REFRESH_TOKEN_EXAMPLE_REF
                    )]
                )]
            )
        ]
    )
    fun reissue(
        @CookieValue(name = REFRESH_TOKEN, required = false)
        @Parameter(description = "재발급용 refreshToken 쿠키")
        refreshToken: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<CommonResponse<Void>> {
        return try {
            val tokens = authService.reissue(refreshToken)

            CookieUtil.addCookie(
                request,
                response,
                ACCESS_TOKEN,
                tokens.accessToken,
                authPolicyConfig.accessTokenTTL.toSeconds().toInt(),
            )

            CookieUtil.addCookie(
                request,
                response,
                REFRESH_TOKEN,
                tokens.refreshToken,
                authPolicyConfig.refreshTokenTTL.toSeconds().toInt(),
            )

            ok(emptySuccess())

        } catch (exception: BadRefreshTokenException) {
            CookieUtil.removeCookie(request, response, ACCESS_TOKEN)
            CookieUtil.removeCookie(request, response, REFRESH_TOKEN)
            throw exception
        }
    }
}
