package jnu.econovation.ecoknockbecentral.airquality.usecase

import jnu.econovation.ecoknockbecentral.airquality.dto.internal.GetTimeseriesDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.internal.GetTimeseriesHistoryDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.internal.AirQualityTimeseriesPointDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.rest.response.GetAirQualityResponse
import jnu.econovation.ecoknockbecentral.common.annotation.UseCase
import org.springframework.data.domain.Slice

@UseCase
interface QueryAirQualityUseCase {
    fun queryAirQuality(): GetAirQualityResponse

    fun queryAirQualityTimeseries(dto: GetTimeseriesDTO): Slice<AirQualityTimeseriesPointDTO>

    fun queryAirQualityTimeseriesHistory(dto: GetTimeseriesHistoryDTO): Slice<AirQualityTimeseriesPointDTO>
}
