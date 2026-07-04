package jnu.econovation.ecoknockbecentral.airquality.dto.internal

data class AirQualityDTO(
    val pm25: Int,
    val humidity: Double,
    val temperature: Double,
    val estimatedEco2PPM: Double,
    val estimatedBvocPPM: Double,
    val accuracy: Int
)