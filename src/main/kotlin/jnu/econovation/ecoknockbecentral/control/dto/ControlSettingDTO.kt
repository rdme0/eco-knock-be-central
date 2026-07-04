package jnu.econovation.ecoknockbecentral.control.dto

import jnu.econovation.ecoknockbecentral.control.model.entity.ControlSetting

data class ControlSettingDTO(
    val enabled: Boolean,
    val darkLuxThreshold: Double,
    val brightLuxThreshold: Double,
    val darkDetectionTimeThreshold: Int,
    val brightDetectionTimeThreshold: Int,
    val airQualityDetectionTimeThreshold: Int,
    val badAirQualityRatioThreshold: Int,
    val cleanAirQualityRatioThreshold: Int,
    val cooldownMinutes: Int,
) {
    companion object {
        fun from(entity: ControlSetting): ControlSettingDTO {
            return ControlSettingDTO(
                enabled = entity.isEnabled,
                darkLuxThreshold = entity.darkLuxThreshold,
                brightLuxThreshold = entity.brightLuxThreshold,
                darkDetectionTimeThreshold = entity.darkDetectionTimeThreshold,
                brightDetectionTimeThreshold = entity.brightDetectionTimeThreshold,
                airQualityDetectionTimeThreshold = entity.airQualityDetectionTimeThreshold,
                badAirQualityRatioThreshold = entity.badAirQualityRatioThreshold,
                cleanAirQualityRatioThreshold = entity.cleanAirQualityRatioThreshold,
                cooldownMinutes = entity.cooldownMinutes,
            )
        }
    }
}
