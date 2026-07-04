package jnu.econovation.ecoknockbecentral.airquality.dto.grpc

import jnu.econovation.ecoknockbecentral.grpc.airpurifier.v1.GetCurrentAirPurifierResponse
import java.time.Instant

data class RawAirPurifierDTO(
    val power: String,
    val isOn: Boolean,
    val aqi: Int,
    val averageAqi: Int,
    val humidity: Int,
    val temperatureC: Double?,
    val mode: String,
    val favoriteLevel: Int,
    val filterLifeRemaining: Int,
    val filterHoursUsed: Int,
    val motorSpeed: Int,
    val purifyVolume: Int,
    val led: Boolean,
    val ledBrightness: Int?,
    val buzzer: Boolean?,
    val childLock: Boolean,
    val measuredAt: Instant
) {
    companion object {
        fun from(response: GetCurrentAirPurifierResponse): RawAirPurifierDTO {
            return RawAirPurifierDTO(
                power = response.power,
                isOn = response.isOn,
                aqi = response.aqi,
                averageAqi = response.averageAqi,
                humidity = response.humidity,
                temperatureC = if (response.hasTemperatureC()) response.temperatureC.value else null,
                mode = response.mode,
                favoriteLevel = response.favoriteLevel,
                filterLifeRemaining = response.filterLifeRemaining,
                filterHoursUsed = response.filterHoursUsed,
                motorSpeed = response.motorSpeed,
                purifyVolume = response.purifyVolume,
                led = response.led,
                ledBrightness = if (response.hasLedBrightness()) response.ledBrightness.value else null,
                buzzer = if (response.hasBuzzer()) response.buzzer.value else null,
                childLock = response.childLock,
                measuredAt = Instant.ofEpochMilli(response.measuredAtUnixMs),
            )
        }
    }
}

data class RawSensorDTO(
    val temperatureC: Double,
    val humidityRh: Double,
    val gasResistanceOhm: Double,
    val status: Int,
    val gasValid: Boolean,
    val heatStable: Boolean,
    val measuredAt: Instant,
    val staticIaq: Double,
    val estimatedEco2PPM: Double,
    val estimatedBvocPPM: Double,
    val accuracy: Int,
    val stabilizationProgressPercent: Int,
    val gasPercentage: Double,
    val learningCompleteAt: Instant
)
