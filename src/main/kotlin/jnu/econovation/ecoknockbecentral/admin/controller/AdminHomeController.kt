package jnu.econovation.ecoknockbecentral.admin.controller

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
@Hidden
class AdminHomeController {
    @GetMapping("/admin", "/admin/")
    @PreAuthorize("hasRole('ADMIN')")
    fun home(): String {
        return "admin/index"
    }
}
