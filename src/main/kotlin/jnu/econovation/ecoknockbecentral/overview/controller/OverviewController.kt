package jnu.econovation.ecoknockbecentral.overview.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.emptySuccess
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.success
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_MEANING_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_MEANING_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.EMPTY_SUCCESS_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.EMPTY_SUCCESS_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.ACCESS_TOKEN_SECURITY_SCHEME_NAME
import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails
import jnu.econovation.ecoknockbecentral.overview.dto.request.UpdateOverviewShortcutRequest
import jnu.econovation.ecoknockbecentral.overview.dto.response.GetShortcutsResponse
import jnu.econovation.ecoknockbecentral.overview.service.OverviewService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/overview/shortcuts")
@Tag(name = "Overview", description = "사용자 모아두기 바로가기 API")
@SecurityRequirement(name = ACCESS_TOKEN_SECURITY_SCHEME_NAME)
class OverviewController(private val service: OverviewService) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "내 모아두기 바로가기 조회",
        description = "현재 로그인한 사용자 또는 게스트의 모아두기 gridSize(2 또는 3)와 바로가기 목록을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF)]
                )]
            )
        ]
    )
    fun getShortcuts(
        @AuthenticationPrincipal userDetails: EcoKnockUserDetails
    ): ResponseEntity<CommonResponse<GetShortcutsResponse>> {
        return ok(success(service.getOverviewShortcuts(memberInfo = userDetails.memberInfo)))
    }

    @PutMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "내 모아두기 바로가기 수정",
        description = "현재 로그인한 사용자 또는 게스트의 모아두기 바로가기 목록을 요청 body 기준으로 교체합니다.",
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
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content(
                    mediaType = "application/json",
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
            ),
        ]
    )
    fun updateShortcuts(
        @AuthenticationPrincipal userDetails: EcoKnockUserDetails,
        @RequestBody request: UpdateOverviewShortcutRequest
    ): ResponseEntity<CommonResponse<Void>> {
        service.updateOverviewShortcuts(memberInfo = userDetails.memberInfo, updateRequest = request)
        return ok(emptySuccess())
    }

    @PutMapping("/reset", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "내 모아두기 바로가기 초기화",
        description = "현재 로그인한 사용자 또는 게스트의 모아두기 바로가기를 기본값으로 초기화합니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "초기화 성공",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(
                        name = EMPTY_SUCCESS_EXAMPLE_NAME,
                        ref = EMPTY_SUCCESS_EXAMPLE_REF
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF)]
                )]
            ),
        ]
    )
    fun resetShortcutsToDefault(
        @AuthenticationPrincipal userDetails: EcoKnockUserDetails
    ): ResponseEntity<CommonResponse<Void>> {
        service.initializeOverview(memberId = userDetails.memberInfo.id)
        return ok(emptySuccess())
    }

}
