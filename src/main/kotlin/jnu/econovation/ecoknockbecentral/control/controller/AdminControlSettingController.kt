package jnu.econovation.ecoknockbecentral.control.controller

import io.swagger.v3.oas.annotations.Hidden
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.success
import jnu.econovation.ecoknockbecentral.control.dto.request.UpdateControlEnabledRequest
import jnu.econovation.ecoknockbecentral.control.dto.request.UpdateControlSettingRequest
import jnu.econovation.ecoknockbecentral.control.dto.response.ControlSettingResponse
import jnu.econovation.ecoknockbecentral.control.service.ControlSettingService
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Hidden
@Controller
@RequestMapping("/admin/control-settings")
@PreAuthorize("hasRole('ADMIN')")
class AdminControlSettingController(
    private val controlSettingService: ControlSettingService,
) {
    companion object {
        private const val CONTROL_SETTINGS_VIEW = "admin/control-settings"
    }

    @GetMapping
    fun editControlSettings(): String {
        return CONTROL_SETTINGS_VIEW
    }

    @GetMapping("/value", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun getControlSettings(): CommonResponse<ControlSettingResponse> {
        return success(controlSettingService.getSettingForResponse())
    }

    @GetMapping("/default-value", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun getDefaultControlSettings(): CommonResponse<ControlSettingResponse> {
        return success(controlSettingService.getDefaultSettingForResponse())
    }

    @PutMapping(
        "/enabled",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @ResponseBody
    fun updateControlEnabled(
        @RequestBody request: UpdateControlEnabledRequest,
    ): CommonResponse<ControlSettingResponse> {
        return success(controlSettingService.updateEnabled(request))
    }

    @PutMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @ResponseBody
    fun updateControlSettings(
        @RequestBody request: UpdateControlSettingRequest,
    ): CommonResponse<ControlSettingResponse> {
        return success(controlSettingService.updateSetting(request))
    }
}
