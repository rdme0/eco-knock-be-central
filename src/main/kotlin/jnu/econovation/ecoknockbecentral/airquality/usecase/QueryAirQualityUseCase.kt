package jnu.econovation.ecoknockbecentral.airquality.usecase

import jnu.econovation.ecoknockbecentral.airquality.dto.GetTimeseriesDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.GetTimeseriesHistoryDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.AirQualityTimeseriesPointDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.response.GetAirQualityResponse
import jnu.econovation.ecoknockbecentral.common.annotation.UseCase
import org.springframework.data.domain.Slice

@UseCase
interface QueryAirQualityUseCase {
    fun queryAirQuality(): GetAirQualityResponse

    fun queryAirQualityTimeseries(dto: GetTimeseriesDTO): Slice<AirQualityTimeseriesPointDTO>

    fun queryAirQualityTimeseriesHistory(dto: GetTimeseriesHistoryDTO): Slice<AirQualityTimeseriesPointDTO>
}
