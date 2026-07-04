package jnu.econovation.ecoknockbecentral.control.dto.response

import jnu.econovation.ecoknockbecentral.control.dto.ControlSettingDTO
import jnu.econovation.ecoknockbecentral.control.model.entity.ControlSetting

data class ControlSettingResponse(
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
        fun from(internalDTO: ControlSettingDTO): ControlSettingResponse {
            return ControlSettingResponse(
                enabled = internalDTO.enabled,
                darkLuxThreshold = internalDTO.darkLuxThreshold,
                brightLuxThreshold = internalDTO.brightLuxThreshold,
                darkDetectionTimeThreshold = internalDTO.darkDetectionTimeThreshold,
                brightDetectionTimeThreshold = internalDTO.brightDetectionTimeThreshold,
                airQualityDetectionTimeThreshold = internalDTO.airQualityDetectionTimeThreshold,
                badAirQualityRatioThreshold = internalDTO.badAirQualityRatioThreshold,
                cleanAirQualityRatioThreshold = internalDTO.cleanAirQualityRatioThreshold,
                cooldownMinutes = internalDTO.cooldownMinutes,
            )
        }

        fun from(entity: ControlSetting): ControlSettingResponse {
            return ControlSettingResponse(
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
