package jnu.econovation.ecoknockbecentral.reward.controller;

import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.ACCESS_TOKEN_SECURITY_SCHEME_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_MEANING_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_MEANING_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.INTERNAL_SERVER_ERROR_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.INTERNAL_SERVER_ERROR_EXAMPLE_REF;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_NAME;
import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.UNAUTHORIZED_EXAMPLE_REF;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse;
import jnu.econovation.ecoknockbecentral.common.security.dto.EcoKnockUserDetails;
import jnu.econovation.ecoknockbecentral.reward.dto.request.RewardHistoryRequest;
import jnu.econovation.ecoknockbecentral.reward.dto.response.RewardHistoryResponse;
import jnu.econovation.ecoknockbecentral.reward.service.RewardHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rewards")
@Tag(name = "Reward", description = "보상 API")
@SecurityRequirement(name = ACCESS_TOKEN_SECURITY_SCHEME_NAME)
@RequiredArgsConstructor
public class RewardController {

    private final RewardHistoryService rewardHistoryService;

    @GetMapping(value = "/me/history", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "내 코인 로그 조회",
            description = "인증된 회원의 확정된 코인 로그를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "코인 로그 조회 성공"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "요청 형식이 올바르지 않음",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = BAD_DATA_SYNTAX_EXAMPLE_NAME,
                                            ref = BAD_DATA_SYNTAX_EXAMPLE_REF
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 필요",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = UNAUTHORIZED_EXAMPLE_NAME,
                                            ref = UNAUTHORIZED_EXAMPLE_REF
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "page 또는 size 값이 유효하지 않음",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = BAD_DATA_MEANING_EXAMPLE_NAME,
                                            ref = BAD_DATA_MEANING_EXAMPLE_REF
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "코인 로그 조회 중 서버 오류",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(
                                            name = INTERNAL_SERVER_ERROR_EXAMPLE_NAME,
                                            ref = INTERNAL_SERVER_ERROR_EXAMPLE_REF
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<CommonResponse<Page<RewardHistoryResponse>>> getMyRewardHistory(
            @Parameter(
                    description = "조회할 페이지 번호(0부터 시작)",
                    example = "0",
                    schema = @Schema(type = "integer", minimum = "0", defaultValue = "0")
            )
            @RequestParam(defaultValue = "0") int page,
            @Parameter(
                    description = "페이지 크기(최대 100)",
                    example = "20",
                    schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "20")
            )
            @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true)
            @AuthenticationPrincipal EcoKnockUserDetails userDetails
    ) {
        RewardHistoryRequest request = new RewardHistoryRequest(page, size);
        Page<RewardHistoryResponse> response = rewardHistoryService.getMyHistory(
                userDetails.memberInfo().getId(),
                request
        );
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
