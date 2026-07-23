package jnu.econovation.ecoknockbecentral.wallet.controller;

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
@Tag(name = "Wallet", description = "인증된 회원의 지갑, KRT 잔액 및 보유량 랭킹 API")
@SecurityRequirement(name = ACCESS_TOKEN_SECURITY_SCHEME_NAME)
@RequiredArgsConstructor
public class WalletController {

    private final WalletBalanceService walletBalanceService;
    private final WalletRankingService walletRankingService;

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "내 지갑 및 KRT 잔액 조회",
            description = """
                    accessToken 쿠키로 인증된 회원의 활성 보상 지갑을 조회하고,
                    해당 지갑 주소의 온체인 KRT 잔액을 사람이 읽을 수 있는 단위로 반환합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "활성 보상 지갑 주소, 지갑 유형, KRT 잔액 및 토큰 심볼 조회 성공",
                            useReturnTypeSchema = true
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
                            responseCode = "404",
                            description = "인증된 회원에게 활성 보상 지갑이 등록되어 있지 않음 (WALLET_404_001)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "지갑 조회, Sepolia RPC 또는 KRT 컨트랙트 호출 중 오류가 발생함 (COMMON_500_001)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    )
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
            description = """
                    accessToken 쿠키로 인증된 회원이 요청한 최대 limit명의 KRT 보유량 랭킹을 조회합니다.
                    게스트, 활성 보상 지갑이 없는 회원, KRT 잔액이 0인 회원은 집계 대상에서 제외되며,
                    동일 잔액은 회원 ID 오름차순으로 순위를 결정합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "토큰 심볼, 랭킹 계산 시각 및 회원별 순위·이름·기수·KRT 잔액 조회 성공",
                            useReturnTypeSchema = true
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "limit을 정수로 변환할 수 없음 (COMMON_400_001)",
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
                            description = "limit이 허용 범위인 1 이상 10 이하를 벗어남 (COMMON_422_001)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "지갑·회원 조회, Redis 캐시 또는 온체인 KRT 잔액 조회 중 오류가 발생함 (COMMON_500_001)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<CommonResponse<WalletRankingResponse>> getWalletRankings(
            @Parameter(
                    name = "limit",
                    description = "반환할 최대 상위 랭킹 인원 수. 생략하면 최대 3명을 반환합니다.",
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
