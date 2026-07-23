package jnu.econovation.ecoknockbecentral.wallet.controller;

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
import jnu.econovation.ecoknockbecentral.wallet.dto.request.WalletRankingRequest;
import jnu.econovation.ecoknockbecentral.wallet.dto.response.WalletBalanceResponse;
import jnu.econovation.ecoknockbecentral.wallet.dto.response.WalletRankingResponse;
import jnu.econovation.ecoknockbecentral.wallet.service.WalletBalanceService;
import jnu.econovation.ecoknockbecentral.wallet.service.WalletRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
@Tag(name = "Wallet", description = "회원 지갑 및 KRT 잔액 API")
@SecurityRequirement(name = ACCESS_TOKEN_SECURITY_SCHEME_NAME)
@RequiredArgsConstructor
public class WalletController {

    private final WalletBalanceService walletBalanceService;
    private final WalletRankingService walletRankingService;

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "내 지갑 및 KRT 잔액 조회",
            description = "로그인한 SSO 회원의 지갑 주소 내 KRT 토큰 보유량 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
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
                    @ApiResponse(responseCode = "404", description = "활성 보상 지갑 없음"),
                    @ApiResponse(responseCode = "500", description = "온체인 잔액 조회 실패")
            }
    )
    public ResponseEntity<CommonResponse<WalletBalanceResponse>> getMyWalletBalance(
            @Parameter(hidden = true)
            @AuthenticationPrincipal EcoKnockUserDetails userDetails
    ) {
        WalletBalanceResponse response = walletBalanceService.getWalletBalance(userDetails.memberInfo().getId());
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @GetMapping(value = "/rankings", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "KRT 보유량 랭킹 조회",
            description = "활성 보상 지갑의 KRT 보유량 랭킹을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "랭킹 조회 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "limit 요청 형식이 올바르지 않음",
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
                            description = "limit은 1 이상 10 이하여야 함",
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
                            description = "랭킹 계산 또는 조회 중 서버 오류",
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
    public ResponseEntity<CommonResponse<WalletRankingResponse>> getWalletRankings(
            @Parameter(
                    description = "반환할 랭킹 개수",
                    example = "3",
                    schema = @Schema(
                            type = "integer",
                            minimum = "1",
                            maximum = "10",
                            defaultValue = "3"
                    )
            )
            @RequestParam(defaultValue = "3") int limit
    ) {
        WalletRankingResponse response = walletRankingService.getRankings(new WalletRankingRequest(limit));
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
