package jnu.econovation.ecoknockbecentral.auth.controller

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
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth/reissue")
class AuthController(
    private val authService: AuthService,
    private val authPolicyConfig: AuthPolicyConfig,
) {
    @PostMapping
    fun reissue(
        @CookieValue(name = REFRESH_TOKEN, required = false)
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
                authPolicyConfig.accessTokenTTL().toSeconds().toInt(),
            )

            CookieUtil.addCookie(
                request,
                response,
                REFRESH_TOKEN,
                tokens.refreshToken,
                authPolicyConfig.refreshTokenTTL().toSeconds().toInt(),
            )

            ok(emptySuccess())

        } catch (exception: BadRefreshTokenException) {
            CookieUtil.removeCookie(request, response, ACCESS_TOKEN)
            CookieUtil.removeCookie(request, response, REFRESH_TOKEN)
            throw exception
        }
    }
}
