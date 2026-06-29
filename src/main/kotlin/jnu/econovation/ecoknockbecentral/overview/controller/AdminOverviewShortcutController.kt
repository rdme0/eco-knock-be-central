package jnu.econovation.ecoknockbecentral.overview.controller

import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.overview.dto.request.ReplaceDefaultOverviewShortcutsRequest
import jnu.econovation.ecoknockbecentral.overview.dto.request.UpdateDefaultShortcutDTO
import jnu.econovation.ecoknockbecentral.overview.service.OverviewService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin")
class AdminOverviewShortcutController(
    private val overviewService: OverviewService,
) {
    companion object {
        private const val OVERVIEW_SHORTCUTS_VIEW = "admin/overview-shortcuts"
    }

    @GetMapping("/overview-shortcuts")
    @PreAuthorize("hasRole('ADMIN')")
    fun editOverviewShortcuts(model: Model): String {
        if (!model.containsAttribute("rows")) {
            model.addAttribute(
                "rows",
                overviewService.getDefaultOverviewShortcuts()
                    .map(transform = UpdateDefaultShortcutDTO::from)
            )
        }

        return OVERVIEW_SHORTCUTS_VIEW
    }

    @PostMapping("/overview-shortcuts")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    fun updateOverviewShortcuts(
        @RequestBody request: ReplaceDefaultOverviewShortcutsRequest,
    ): ResponseEntity<CommonResponse<Void>> {
        overviewService.replaceDefaultOverviewShortcuts(request)

        return ResponseEntity.ok(CommonResponse.emptySuccess())
    }
}
