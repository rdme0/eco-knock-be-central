package jnu.econovation.ecoknockbecentral.control.dto.request

import jnu.econovation.ecoknockbecentral.common.exception.client.BadDataMeaningException

data class UpdateControlSettingRequest(
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
    init {
        when {
            darkLuxThreshold !in 0.0..1000.0 -> {
                throw BadDataMeaningException("어두움 lux 기준은 0.0 이상 1000.0 이하만 가능합니다.")
            }

            brightLuxThreshold !in 0.0..1000.0 -> {
                throw BadDataMeaningException("밝음 lux 기준은 0.0 이상 1000.0 이하만 가능합니다.")
            }

            darkDetectionTimeThreshold !in 1..1440 -> {
                throw BadDataMeaningException("어두움 지속 시간은 1분 이상 1440분 이하만 가능합니다.")
            }

            brightDetectionTimeThreshold !in 1..60 -> {
                throw BadDataMeaningException("밝음 지속 시간은 1분 이상 60분 이하만 가능합니다.")
            }

            airQualityDetectionTimeThreshold !in 1..60 -> {
                throw BadDataMeaningException("공기질 확인 구간은 1분 이상 60분 이하만 가능합니다.")
            }

            badAirQualityRatioThreshold !in 1..100 -> {
                throw BadDataMeaningException("나쁜 공기질 비율은 1% 이상 100% 이하만 가능합니다.")
            }

            cleanAirQualityRatioThreshold !in 1..100 -> {
                throw BadDataMeaningException("깨끗한 공기질 비율은 1% 이상 100% 이하만 가능합니다.")
            }

            cooldownMinutes !in 0..1440 -> {
                throw BadDataMeaningException("자동제어 쿨타임은 0분 이상 1440분 이하만 가능합니다.")
            }
        }
    }
}
