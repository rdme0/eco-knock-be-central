package jnu.econovation.ecoknockbecentral.airquality.dto.internal

import jnu.econovation.ecoknockbecentral.airquality.readmodel.entity.AirQualityView
import jnu.econovation.ecoknockbecentral.common.extension.toZonedDateTime
import org.springframework.data.domain.Slice
import java.time.ZonedDateTime

typealias AirQualityTimeseriesSlice = Slice<AirQualityTimeseriesPointDTO>

data class AirQualityTimeseriesPointDTO(
    val time: ZonedDateTime,
    val end: ZonedDateTime,
    val pm25Quality: Quality,
    val humidity: Double,
    val temperature: Double,
    val gasQuality: Quality,
    val sampleCount: Long,
) {
    companion object {
        fun from(entity: AirQualityView): AirQualityTimeseriesPointDTO {
            return AirQualityTimeseriesPointDTO(
                time = entity.bucketStart.toZonedDateTime(),
                end = entity.bucketEnd.toZonedDateTime(),
                pm25Quality = Quality.fromPm25(entity.avgPm25),
                humidity = entity.avgHumidity,
                temperature = entity.avgTemperature,
                gasQuality = Quality.fromGas(
                    eco2 = entity.avgEco2,
                    bvoc = entity.avgBvoc,
                ),
                sampleCount = entity.sampleCount,
            )
        }
    }
}
