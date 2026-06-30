package jnu.econovation.ecoknockbecentral.airquality.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jnu.econovation.ecoknockbecentral.airquality.dto.request.GetTimeseriesHistoryRequest
import jnu.econovation.ecoknockbecentral.airquality.dto.request.GetTimeseriesRequest
import jnu.econovation.ecoknockbecentral.airquality.dto.response.AirQualityTimeseriesSlice
import jnu.econovation.ecoknockbecentral.airquality.usecase.QueryAirQualityUseCase
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.success
import jnu.econovation.ecoknockbecentral.common.openapi.OpenApiConstants.AIR_QUALITY_BAD_REQUEST_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.OpenApiConstants.AIR_QUALITY_BAD_REQUEST_EXAMPLE_REF
import jnu.econovation.ecoknockbecentral.common.openapi.OpenApiConstants.AIR_QUALITY_HISTORY_LIMIT_EXAMPLE_NAME
import jnu.econovation.ecoknockbecentral.common.openapi.OpenApiConstants.AIR_QUALITY_HISTORY_LIMIT_EXAMPLE_REF
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/air-quality/timeseries")
@Tag(name = "Air Quality", description = "공기질 시계열 API")
class AirQualityTimeseriesController(
    private val queryAirQualityUseCase: QueryAirQualityUseCase,
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "공기질 시계열 조회",
        description = "resolution, from, to 기준으로 공기질 그래프 포인트를 조회합니다.",
        parameters = [
            Parameter(
                name = "resolution",
                `in` = ParameterIn.QUERY,
                required = true,
                description = "집계 해상도",
                schema = Schema(type = "string", allowableValues = ["1m", "5m", "15m", "1h", "4h", "1d"]),
                example = "5m",
            ),
            Parameter(
                name = "from",
                `in` = ParameterIn.QUERY,
                required = true,
                description = "조회 시작 시각",
                schema = Schema(type = "string", format = "date-time"),
                example = "2026-06-30T00:00:00Z",
            ),
            Parameter(
                name = "to",
                `in` = ParameterIn.QUERY,
                required = true,
                description = "조회 종료 시각",
                schema = Schema(type = "string", format = "date-time"),
                example = "2026-06-30T01:00:00Z",
            )
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(
                responseCode = "400",
                description = "공기질 조회 파라미터 오류",
                content = [Content(
                    mediaType = "application/json",
                    examples = [ExampleObject(
                        name = AIR_QUALITY_BAD_REQUEST_EXAMPLE_NAME,
                        ref = AIR_QUALITY_BAD_REQUEST_EXAMPLE_REF
                    )]
                )]
            )
        ]
    )
    fun timeseries(
        @Parameter(hidden = true)
        request: GetTimeseriesRequest
    ): ResponseEntity<CommonResponse<AirQualityTimeseriesSlice>> {
        return ok(success(queryAirQualityUseCase.queryAirQualityTimeseries(request)))
    }

    @GetMapping("/history", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "공기질 과거 시계열 조회",
        description = "before 이전 공기질 그래프 포인트를 최신순 기준 limit 만큼 조회합니다.",
        parameters = [
            Parameter(
                name = "resolution",
                `in` = ParameterIn.QUERY,
                required = true,
                description = "집계 해상도",
                schema = Schema(type = "string", allowableValues = ["1m", "5m", "15m", "1h", "4h", "1d"]),
                example = "5m",
            ),
            Parameter(
                name = "before",
                `in` = ParameterIn.QUERY,
                required = true,
                description = "이 시각 이전 데이터 조회",
                schema = Schema(type = "string", format = "date-time"),
                example = "2026-06-30T01:00:00Z",
            ),
            Parameter(
                name = "limit",
                `in` = ParameterIn.QUERY,
                required = true,
                description = "조회 개수. 1 이상 500 이하",
                schema = Schema(type = "integer", format = "int32", minimum = "1", maximum = "500"),
                example = "50",
            )
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(
                responseCode = "400",
                description = "공기질 히스토리 조회 파라미터 오류",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                name = AIR_QUALITY_BAD_REQUEST_EXAMPLE_NAME,
                                ref = AIR_QUALITY_BAD_REQUEST_EXAMPLE_REF
                            ),
                            ExampleObject(
                                name = AIR_QUALITY_HISTORY_LIMIT_EXAMPLE_NAME,
                                ref = AIR_QUALITY_HISTORY_LIMIT_EXAMPLE_REF
                            )
                        ]
                    )
                ]
            )
        ]
    )
    fun history(
        @Parameter(hidden = true)
        request: GetTimeseriesHistoryRequest
    ): ResponseEntity<CommonResponse<AirQualityTimeseriesSlice>> {
        return ok(success(queryAirQualityUseCase.queryAirQualityTimeseriesHistory(request)))
    }
}
