package jnu.econovation.ecoknockbecentral.overview.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.emptySuccess
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.ACCESS_TOKEN_SECURITY_SCHEME_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.EMPTY_SUCCESS_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.EMPTY_SUCCESS_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.OVERVIEW_LAYOUT_GRID_SIZE_CONFLICT_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.OVERVIEW_LAYOUT_GRID_SIZE_CONFLICT_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails
import jnu.econovation.ecoknockbecentral.overview.dto.request.UpdateOverviewLayoutRequest
import jnu.econovation.ecoknockbecentral.overview.service.OverviewService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/overview/layout")
@Tag(name = "Overview", description = "사용자 모아두기 레이아웃 API")
@SecurityRequirement(name = ACCESS_TOKEN_SECURITY_SCHEME_NAME)
class OverviewLayoutController(
    private val overviewService: OverviewService,
) {
    @PutMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "내 모아두기 그리드 크기 수정",
        description = "현재 로그인한 사용자 또는 게스트의 모아두기 그리드 크기를 2 또는 3으로 변경합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = [ExampleObject(name = EMPTY_SUCCESS_EXAMPLE_NAME, ref = EMPTY_SUCCESS_EXAMPLE_REF)])],
            ),
            ApiResponse(
                responseCode = "400",
                description = "지원하지 않는 gridSize",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = [ExampleObject(name = BAD_DATA_SYNTAX_EXAMPLE_NAME, ref = BAD_DATA_SYNTAX_EXAMPLE_REF)])],
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = [ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF)])],
            ),
            ApiResponse(
                responseCode = "409",
                description = "현재 gridSize와 동일",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = [ExampleObject(name = OVERVIEW_LAYOUT_GRID_SIZE_CONFLICT_EXAMPLE_NAME, ref = OVERVIEW_LAYOUT_GRID_SIZE_CONFLICT_EXAMPLE_REF)])],
            ),
        ],
    )
    fun updateLayout(
        @AuthenticationPrincipal userDetails: EcoKnockUserDetails,
        @RequestBody request: UpdateOverviewLayoutRequest,
    ): ResponseEntity<CommonResponse<Void>> {
        overviewService.updateOverviewLayout(userDetails.memberInfo, request)
        return ok(emptySuccess())
    }
}
