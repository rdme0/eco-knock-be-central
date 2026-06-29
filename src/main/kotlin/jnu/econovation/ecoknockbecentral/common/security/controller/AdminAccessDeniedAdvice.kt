package jnu.econovation.ecoknockbecentral.common.security.controller

import jnu.econovation.ecoknockbecentral.overview.controller.AdminOverviewShortcutController
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.ModelAndView

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(assignableTypes = [AdminOverviewShortcutController::class])
class AdminAccessDeniedAdvice {

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationDenied(): ModelAndView {
        return ModelAndView("admin/access-denied", HttpStatus.FORBIDDEN)
    }
}
