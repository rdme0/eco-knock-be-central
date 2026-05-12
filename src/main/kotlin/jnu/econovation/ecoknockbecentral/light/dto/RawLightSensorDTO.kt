package jnu.econovation.ecoknockbecentral.light.dto

import java.time.Instant

data class RawLightSensorDTO(
    val lux: Double,
    val measuredAt: Instant,

    //for debugging
    val rawAls: Int,
    val rawWhite: Int
)