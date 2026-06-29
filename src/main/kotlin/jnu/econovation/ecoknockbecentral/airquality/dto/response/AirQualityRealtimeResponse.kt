package jnu.econovation.ecoknockbecentral.airquality.dto.response

import jnu.econovation.ecoknockbecentral.airquality.command.SaveAirQualityCommand
import jnu.econovation.ecoknockbecentral.airquality.dto.Quality
import jnu.econovation.ecoknockbecentral.common.extension.toZonedDateTime
import java.time.ZonedDateTime

data class AirQualityRealtimeResponse(
    val measuredAt: ZonedDateTime,
    val pm25Quality: Quality,
    val humidity: Double,
    val temperature: Double,
    val gasQuality: Quality,
    val accuracy: Int,
) {
    companion object {
        fun from(command: SaveAirQualityCommand): AirQualityRealtimeResponse {
            val airQuality = command.airQuality

            return AirQualityRealtimeResponse(
                measuredAt = command.rawSensor.measuredAt.toZonedDateTime(),
                pm25Quality = Quality.fromPm25(airQuality.pm25),
                humidity = airQuality.humidity,
                temperature = airQuality.temperature,
                gasQuality = Quality.fromGas(
                    eco2 = airQuality.estimatedEco2PPM,
                    bvoc = airQuality.estimatedBvocPPM,
                ),
                accuracy = airQuality.accuracy,
            )
        }
    }
}
