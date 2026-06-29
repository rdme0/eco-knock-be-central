package jnu.econovation.ecoknockbecentral.overview.controller

import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.emptySuccess
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.success
import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails
import jnu.econovation.ecoknockbecentral.overview.dto.request.UpdateOverviewShortcutRequest
import jnu.econovation.ecoknockbecentral.overview.dto.response.GetShortcutsResponse
import jnu.econovation.ecoknockbecentral.overview.service.OverviewService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/overview/shortcuts")
class OverviewController(private val service: OverviewService) {

    @GetMapping
    fun getShortcuts(
        @AuthenticationPrincipal userDetails: EcoKnockUserDetails
    ): ResponseEntity<CommonResponse<GetShortcutsResponse>> {
        return ok(success(service.getOverviewShortcuts(memberInfo = userDetails.memberInfo)))
    }

    @PutMapping
    fun updateShortcuts(
        @AuthenticationPrincipal userDetails: EcoKnockUserDetails,
        @RequestBody request: UpdateOverviewShortcutRequest
    ): ResponseEntity<CommonResponse<Void>> {
        service.updateOverviewShortcuts(memberInfo = userDetails.memberInfo, updateRequest = request)
        return ok(emptySuccess())
    }

    @PutMapping("/reset")
    fun resetShortcutsToDefault(
        @AuthenticationPrincipal userDetails: EcoKnockUserDetails
    ): ResponseEntity<CommonResponse<Void>> {
        service.initOverviewShortcuts(memberId = userDetails.memberInfo.id)
        return ok(emptySuccess())
    }

}
