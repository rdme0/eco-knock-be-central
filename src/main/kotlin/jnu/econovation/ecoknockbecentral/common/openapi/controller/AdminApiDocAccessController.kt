package jnu.econovation.ecoknockbecentral.common.openapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.success
import jnu.econovation.ecoknockbecentral.common.openapi.dto.ApiDocAccessResponse
import jnu.econovation.ecoknockbecentral.common.openapi.dto.UpdateApiDocAccessRequest
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.ACCESS_TOKEN_SECURITY_SCHEME_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.service.ApiDocAccessService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/api-docs-access")
@Tag(name = "Admin", description = "관리자 JSON API")
@SecurityRequirement(name = ACCESS_TOKEN_SECURITY_SCHEME_NAME)
class AdminApiDocAccessController(
    private val apiDocAccessService: ApiDocAccessService,
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "API 문서 공개 상태 조회",
        description = "관리자 API 문서의 현재 공개 상태를 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "공개 상태 조회 성공"),
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
    fun getAccess(): CommonResponse<ApiDocAccessResponse> {
        return success(ApiDocAccessResponse(enabled = apiDocAccessService.isEnabled()))
    }

    @PutMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @Operation(
        summary = "API 문서 공개 상태 변경",
        description = "관리자 API 문서의 공개 상태를 변경합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "공개 상태 변경 성공"),
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
    fun updateAccess(
        @RequestBody request: UpdateApiDocAccessRequest,
    ): CommonResponse<ApiDocAccessResponse> {
        return success(apiDocAccessService.update(request.enabled))
    }
}
