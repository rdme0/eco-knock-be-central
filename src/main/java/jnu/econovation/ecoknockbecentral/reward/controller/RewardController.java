package jnu.econovation.ecoknockbecentral.reward.controller;

import static jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants.ACCESS_TOKEN_SECURITY_SCHEME_NAME;
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
import org.springframework.data.web.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rewards")
@Tag(name = "Reward", description = "인증된 회원의 KRT 보상 내역 API")
@SecurityRequirement(name = ACCESS_TOKEN_SECURITY_SCHEME_NAME)
@RequiredArgsConstructor
public class RewardController {

    private final RewardHistoryService rewardHistoryService;

    @GetMapping(value = "/me/history", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "내 코인 로그 조회",
            description = """
                    accessToken 쿠키로 인증된 회원의 확정된 KRT 보상 내역만 페이지 단위로 조회합니다.
                    아직 제출 중이거나 블록체인에서 확정되지 않은 지급 내역은 결과에 포함하지 않습니다.
                    최신 정산 기준일의 내역부터 반환합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "보상 유형, 지급 KRT 수량, 재실 시간, 정산 기준일과 페이지 정보 조회 성공",
                            useReturnTypeSchema = true
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "page 또는 size를 정수로 변환할 수 없음 (COMMON_400_001)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "accessToken 쿠키가 없거나 유효하지 않아 인증할 수 없음",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = UNAUTHORIZED_EXAMPLE_NAME,
                                            ref = UNAUTHORIZED_EXAMPLE_REF
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "인증된 게스트 회원은 접근할 수 없음 (SECURITY_403_001)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "page가 0 미만이거나 size가 1 이상 100 이하 범위를 벗어남 (COMMON_422_001)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "확정된 보상 내역을 데이터베이스에서 조회하는 중 오류가 발생함 (COMMON_500_001)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<CommonResponse<PagedModel<RewardHistoryResponse>>> getMyRewardHistory(
            @Parameter(
                    name = "page",
                    description = "조회할 페이지 번호. 0부터 시작하며 생략하면 첫 페이지를 조회합니다.",
                    example = "0",
                    schema = @Schema(type = "integer", minimum = "0", defaultValue = "0")
            )
            @RequestParam(defaultValue = "0") int page,
            @Parameter(
                    name = "size",
                    description = "한 페이지에 포함할 보상 내역 수. 생략하면 20개를 반환합니다.",
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
        return ResponseEntity.ok(CommonResponse.success(new PagedModel<>(response)));
    }
}
