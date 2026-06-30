package jnu.econovation.ecoknockbecentral.common.security.controller

import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jnu.econovation.ecoknockbecentral.common.cookie.util.CookieUtil
import jnu.econovation.ecoknockbecentral.common.security.constant.AdminAuthConstant.ADMIN_MASTER_TOKEN
import jnu.econovation.ecoknockbecentral.common.security.service.AdminLoginUrlService
import jnu.econovation.ecoknockbecentral.common.security.service.AdminMasterAuthService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin")
@Hidden
class AdminAuthController(
    private val adminMasterAuthService: AdminMasterAuthService,
    private val adminLoginUrlService: AdminLoginUrlService,
) {
    @GetMapping("/login")
    fun login(
        request: HttpServletRequest,
        model: Model,
    ): String {
        model.addAttribute("ssoLoginUrl", adminLoginUrlService.ssoLoginUrl(request))
        return "admin/login"
    }

    @PostMapping("/login/master")
    fun loginWithMasterPassword(
        @RequestParam(name = "password", required = false) password: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
        model: Model,
    ): String {
        val token = adminMasterAuthService.authenticate(password)
        if (token == null) {
            model.addAttribute("ssoLoginUrl", adminLoginUrlService.ssoLoginUrl(request))
            model.addAttribute("errorMessage", "마스터 비밀번호가 올바르지 않습니다.")
            return "admin/login"
        }

        CookieUtil.addCookie(
            request,
            response,
            ADMIN_MASTER_TOKEN,
            token.value,
            token.maxAgeSeconds,
        )

        return "redirect:${AdminLoginUrlService.ADMIN_HOME_PATH}"
    }

    @PostMapping("/logout")
    fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): String {
        CookieUtil.removeCookie(request, response, ADMIN_MASTER_TOKEN)
        return "redirect:/admin/login"
    }

    @GetMapping("/access-denied")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun accessDenied(): String {
        return "admin/access-denied"
    }
}
