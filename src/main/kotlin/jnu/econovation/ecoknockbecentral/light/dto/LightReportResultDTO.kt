package jnu.econovation.ecoknockbecentral.light.dto

import java.time.Instant

data class LightReportResultDTO(
    val sampleCount: Long,
    val minLux: Double?,
    val avgLux: Double?,
    val maxLux: Double?,
    val measuredAt: Instant?
)
