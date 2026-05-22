package jnu.econovation.ecoknockbecentral.airquality.controller

import jnu.econovation.ecoknockbecentral.airquality.dto.request.GetTimeseriesHistoryRequest
import jnu.econovation.ecoknockbecentral.airquality.dto.request.GetTimeseriesRequest
import jnu.econovation.ecoknockbecentral.airquality.dto.response.AirQualityTimeseriesSlice
import jnu.econovation.ecoknockbecentral.airquality.usecase.QueryAirQualityUseCase
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.success
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/air-quality/timeseries")
class AirQualityTimeseriesController(
    private val queryAirQualityUseCase: QueryAirQualityUseCase,
) {
    @GetMapping
    fun timeseries(
        request: GetTimeseriesRequest
    ): ResponseEntity<CommonResponse<AirQualityTimeseriesSlice>> {
        return ok(success(queryAirQualityUseCase.queryAirQualityTimeseries(request)))
    }

    @GetMapping("/history")
    fun history(
        request: GetTimeseriesHistoryRequest
    ): ResponseEntity<CommonResponse<AirQualityTimeseriesSlice>> {
        return ok(success(queryAirQualityUseCase.queryAirQualityTimeseriesHistory(request)))
    }
}