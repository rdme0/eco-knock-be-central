package jnu.econovation.ecoknockbecentral.member.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.success
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.ACCESS_TOKEN_SECURITY_SCHEME_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails
import jnu.econovation.ecoknockbecentral.member.dto.response.GetProfileResponse
import jnu.econovation.ecoknockbecentral.member.service.MemberService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/profile")
@RestController
@Tag(name = "Member", description = "회원 정보 API")
@SecurityRequirement(name = ACCESS_TOKEN_SECURITY_SCHEME_NAME)
class MemberController(
    private val service: MemberService,
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "내 프로필 조회",
        description = "게스트, 일반 회원 또는 관리자로 인증된 accessToken의 역할, 기수, 이름, 활동 상태를 조회합니다. 게스트의 기수와 활동 상태는 null입니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            ApiResponse(
                responseCode = "401",
                description = "인증 필요",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [ExampleObject(name = UNAUTHORIZED_EXAMPLE_NAME, ref = UNAUTHORIZED_EXAMPLE_REF)]
                )]
            )
        ]
    )
    fun getProfile(
        @AuthenticationPrincipal
        userDetails: EcoKnockUserDetails
    ): ResponseEntity<CommonResponse<GetProfileResponse>> {
        return ok(success(service.getProfile(userDetails.memberInfo)))
    }
}
