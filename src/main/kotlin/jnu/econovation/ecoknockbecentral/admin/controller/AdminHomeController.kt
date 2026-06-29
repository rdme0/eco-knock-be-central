package jnu.econovation.ecoknockbecentral.admin.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AdminHomeController {
    @GetMapping("/admin", "/admin/")
    @PreAuthorize("hasRole('ADMIN')")
    fun home(): String {
        return "admin/index"
    }
}
