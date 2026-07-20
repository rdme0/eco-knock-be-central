package jnu.econovation.ecoknockbecentral.overview.controller

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_MEANING_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_MEANING_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.EMPTY_SUCCESS_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.EMPTY_SUCCESS_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.ACCESS_TOKEN_SECURITY_SCHEME_NAME
import jnu.econovation.ecoknockbecentral.overview.dto.request.ReplaceDefaultOverviewShortcutsRequest
import jnu.econovation.ecoknockbecentral.overview.dto.request.UpdateDefaultShortcutDTO
import jnu.econovation.ecoknockbecentral.overview.service.OverviewService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/admin")
@Tag(name = "Admin", description = "관리자 JSON API")
@SecurityRequirement(name = ACCESS_TOKEN_SECURITY_SCHEME_NAME)
class AdminOverviewShortcutController(
    private val overviewService: OverviewService,
) {
    companion object {
        private const val OVERVIEW_SHORTCUTS_VIEW = "admin/overview-shortcuts"
    }

    @GetMapping("/overview-shortcuts")
    @PreAuthorize("hasRole('ADMIN')")
    @Hidden
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

    @PostMapping(
        "/overview-shortcuts",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    @Operation(
        summary = "기본 모아두기 수정",
        description = "모든 사용자에게 적용되는 기본 모아두기 바로가기 목록을 교체합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(
                        name = EMPTY_SUCCESS_EXAMPLE_NAME,
                        ref = EMPTY_SUCCESS_EXAMPLE_REF
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "요청 본문 문법 오류",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(name = BAD_DATA_SYNTAX_EXAMPLE_NAME, ref = BAD_DATA_SYNTAX_EXAMPLE_REF)]
                )]
            ),
            ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = [Content()]),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF)]
                )]
            ),
            ApiResponse(
                responseCode = "422",
                description = "요청 본문 의미 오류",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(name = BAD_DATA_MEANING_EXAMPLE_NAME, ref = BAD_DATA_MEANING_EXAMPLE_REF)]
                )]
            )
        ]
    )
    fun updateOverviewShortcuts(
        @RequestBody request: ReplaceDefaultOverviewShortcutsRequest,
    ): ResponseEntity<CommonResponse<Void>> {
        overviewService.replaceDefaultOverviewShortcuts(request)

        return ResponseEntity.ok(CommonResponse.emptySuccess())
    }
}
