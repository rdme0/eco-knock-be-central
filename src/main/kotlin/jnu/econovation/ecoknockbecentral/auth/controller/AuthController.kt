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
import jnu.econovation.ecoknockbecentral.auth.dto.request.AdminLoginRequest
import jnu.econovation.ecoknockbecentral.auth.exception.BadRefreshTokenException
import jnu.econovation.ecoknockbecentral.auth.service.AuthService
import jnu.econovation.ecoknockbecentral.common.cookie.util.CookieUtil
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.emptySuccess
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_MEANING_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_MEANING_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_REFRESH_TOKEN_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_REFRESH_TOKEN_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.EMPTY_SUCCESS_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.EMPTY_SUCCESS_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GUEST_LOGIN_RATE_LIMIT_EXCEEDED_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.GUEST_LOGIN_RATE_LIMIT_EXCEEDED_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_REF
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.http.ResponseEntity.noContent
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "인증 토큰 API")
class AuthController(
    private val authService: AuthService,
    private val authPolicyConfig: AuthPolicyConfig,
) {
    @PostMapping("/reissue", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "인증 토큰 재발급",
        description = "refreshToken 쿠키를 검증하고 accessToken, refreshToken 쿠키를 재발급합니다.",
        security = [],
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
                if (tokens.isSessionCookie) -1 else authPolicyConfig.accessTokenTTL.toSeconds().toInt(),
            )

            CookieUtil.addCookie(
                request,
                response,
                REFRESH_TOKEN,
                tokens.refreshToken,
                if (tokens.isSessionCookie) -1 else authPolicyConfig.refreshTokenTTL.toSeconds().toInt(),
            )

            ok(emptySuccess())

        } catch (exception: BadRefreshTokenException) {
            CookieUtil.removeCookie(request, response, ACCESS_TOKEN)
            CookieUtil.removeCookie(request, response, REFRESH_TOKEN)
            throw exception
        }
    }

    @PostMapping("/guest")
    @Operation(
        summary = "게스트 로그인",
        description = "임시 게스트 회원을 생성하고 24시간 동안 유효한 세션 쿠키를 발급합니다. 게스트는 프로필·overview 조회와 자신의 overview 수정·초기화·grid size 변경을 사용할 수 있습니다.",
        security = [],
        responses = [
            ApiResponse(responseCode = "204", description = "게스트 로그인 성공", content = [Content()]),
            ApiResponse(
                responseCode = "429",
                description = "IP별 게스트 로그인 횟수 초과",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(
                        name = GUEST_LOGIN_RATE_LIMIT_EXCEEDED_EXAMPLE_NAME,
                        ref = GUEST_LOGIN_RATE_LIMIT_EXCEEDED_EXAMPLE_REF,
                    )]
                )]
            )
        ]
    )
    fun loginAsGuest(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        val tokens = authService.issueGuestToken(request.remoteAddr)

        CookieUtil.addCookie(request, response, ACCESS_TOKEN, tokens.accessToken, -1)
        CookieUtil.addCookie(request, response, REFRESH_TOKEN, tokens.refreshToken, -1)

        return noContent().build()
    }

    @PostMapping(
        "/admin",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
    )
    @Operation(
        summary = "관리자 마스터 로그인",
        description = "관리자 마스터 비밀번호를 검증하고 ID가 0이며 ADMIN 역할인 시스템 관리자 회원의 accessToken, refreshToken HttpOnly 쿠키를 발급합니다.",
        security = [],
        responses = [
            ApiResponse(responseCode = "204", description = "관리자 마스터 로그인 성공", content = [Content()]),
            ApiResponse(
                responseCode = "401",
                description = "마스터 비밀번호 불일치 또는 누락",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(
                        name = UNAUTHORIZED_EXAMPLE_NAME,
                        ref = UNAUTHORIZED_EXAMPLE_REF,
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "422",
                description = "ID 0 시스템 관리자 회원이 없거나 ADMIN 역할이 아님",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(
                        name = BAD_DATA_MEANING_EXAMPLE_NAME,
                        ref = BAD_DATA_MEANING_EXAMPLE_REF,
                    )]
                )]
            ),
        ]
    )
    fun loginWithAdminMasterPassword(
        @RequestBody(required = false) request: AdminLoginRequest?,
        servletRequest: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        val tokens = authService.issueAdminToken(request?.password)

        CookieUtil.addCookie(
            servletRequest,
            response,
            ACCESS_TOKEN,
            tokens.accessToken,
            authPolicyConfig.accessTokenTTL.toSeconds().toInt(),
        )
        CookieUtil.addCookie(
            servletRequest,
            response,
            REFRESH_TOKEN,
            tokens.refreshToken,
            authPolicyConfig.refreshTokenTTL.toSeconds().toInt(),
        )

        return noContent().build()
    }

    @PostMapping("/logout")
    @Operation(
        summary = "로그아웃",
        description = "현재 refreshToken이 활성 세션과 일치하면 서버 세션을 폐기하고 accessToken, refreshToken 쿠키를 삭제합니다.",
        security = [],
        responses = [
            ApiResponse(responseCode = "204", description = "로그아웃 처리 완료", content = [Content()]),
        ],
    )
    fun logout(
        @CookieValue(name = REFRESH_TOKEN, required = false)
        @Parameter(description = "로그아웃할 refreshToken 쿠키")
        refreshToken: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        authService.logout(refreshToken)
        CookieUtil.removeCookie(request, response, ACCESS_TOKEN)
        CookieUtil.removeCookie(request, response, REFRESH_TOKEN)
        return noContent().build()
    }

}
