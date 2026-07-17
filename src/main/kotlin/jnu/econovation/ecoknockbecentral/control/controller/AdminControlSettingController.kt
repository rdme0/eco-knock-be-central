package jnu.econovation.ecoknockbecentral.control.controller

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.success
import jnu.econovation.ecoknockbecentral.control.dto.request.UpdateControlEnabledRequest
import jnu.econovation.ecoknockbecentral.control.dto.request.UpdateControlSettingRequest
import jnu.econovation.ecoknockbecentral.control.dto.response.ControlSettingResponse
import jnu.econovation.ecoknockbecentral.control.service.ControlSettingService
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.ACCESS_TOKEN_SECURITY_SCHEME_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.ADMIN_MASTER_TOKEN_SECURITY_SCHEME_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_MEANING_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_MEANING_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_REF
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin/control-settings")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "관리자 JSON API")
@SecurityRequirements(
    value = [
        SecurityRequirement(name = ACCESS_TOKEN_SECURITY_SCHEME_NAME),
        SecurityRequirement(name = ADMIN_MASTER_TOKEN_SECURITY_SCHEME_NAME),
    ]
)
class AdminControlSettingController(
    private val controlSettingService: ControlSettingService,
) {
    companion object {
        private const val CONTROL_SETTINGS_VIEW = "admin/control-settings"
    }

    @GetMapping
    @Hidden
    fun editControlSettings(): String {
        return CONTROL_SETTINGS_VIEW
    }

    @GetMapping("/value", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Operation(
        summary = "현재 자동제어 설정 조회",
        description = "현재 저장된 공기청정기 자동제어 설정을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "현재 설정 조회 성공"),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF)]
                )]
            ),
            ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = [Content()]),
        ]
    )
    fun getControlSettings(): CommonResponse<ControlSettingResponse> {
        return success(controlSettingService.getSettingForResponse())
    }

    @GetMapping("/default-value", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Operation(
        summary = "기본 자동제어 설정 조회",
        description = "자동제어 설정의 기본값을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "기본 설정 조회 성공"),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF)]
                )]
            ),
            ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = [Content()]),
        ]
    )
    fun getDefaultControlSettings(): CommonResponse<ControlSettingResponse> {
        return success(controlSettingService.getDefaultSettingForResponse())
    }

    @PutMapping(
        "/enabled",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @ResponseBody
    @Operation(
        summary = "자동제어 활성화 상태 변경",
        description = "공기청정기 자동제어의 활성화 상태를 변경합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "활성화 상태 변경 성공"),
            ApiResponse(
                responseCode = "400",
                description = "요청 본문 문법 오류",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(name = BAD_DATA_SYNTAX_EXAMPLE_NAME, ref = BAD_DATA_SYNTAX_EXAMPLE_REF)]
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF)]
                )]
            ),
            ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = [Content()]),
        ]
    )
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
    @Operation(
        summary = "자동제어 설정 변경",
        description = "공기청정기 자동제어 임계값과 시간 설정을 변경합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "자동제어 설정 변경 성공"),
            ApiResponse(
                responseCode = "400",
                description = "요청 본문 문법 오류",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(name = BAD_DATA_SYNTAX_EXAMPLE_NAME, ref = BAD_DATA_SYNTAX_EXAMPLE_REF)]
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF)]
                )]
            ),
            ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = [Content()]),
            ApiResponse(
                responseCode = "422",
                description = "자동제어 설정 값 의미 오류",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(name = BAD_DATA_MEANING_EXAMPLE_NAME, ref = BAD_DATA_MEANING_EXAMPLE_REF)]
                )]
            ),
        ]
    )
    fun updateControlSettings(
        @RequestBody request: UpdateControlSettingRequest,
    ): CommonResponse<ControlSettingResponse> {
        return success(controlSettingService.updateSetting(request))
    }
}
