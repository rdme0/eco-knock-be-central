package jnu.econovation.ecoknockbecentral.control.service

import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import jnu.econovation.ecoknockbecentral.control.constants.ControlSettingConstants
import jnu.econovation.ecoknockbecentral.control.dto.ControlSettingDTO
import jnu.econovation.ecoknockbecentral.control.dto.request.UpdateControlEnabledRequest
import jnu.econovation.ecoknockbecentral.control.dto.request.UpdateControlSettingRequest
import jnu.econovation.ecoknockbecentral.control.dto.response.ControlSettingResponse
import jnu.econovation.ecoknockbecentral.control.model.entity.ControlSetting
import jnu.econovation.ecoknockbecentral.control.repository.ControlSettingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class ControlSettingService(
    private val controlSettingRepository: ControlSettingRepository,
) {
    @Transactional(readOnly = true)
    fun getSetting(): ControlSettingDTO {
        return ControlSettingDTO.from(entity = getSettingEntity())
    }

    @Transactional(readOnly = true)
    fun getSettingForResponse(): ControlSettingResponse {
        return ControlSettingResponse.from(internalDTO = getSetting())
    }

    @Transactional(readOnly = true)
    fun getDefaultSettingForResponse(): ControlSettingResponse {
        return ControlSettingResponse.from(entity = ControlSettingConstants.toEntity())
    }

    @Transactional
    fun initializeDefaultSetting() {
        controlSettingRepository.save(ControlSettingConstants.toEntity())
    }

    @Transactional
    fun updateSetting(request: UpdateControlSettingRequest): ControlSettingResponse {
        val entity: ControlSetting = getSettingEntity()

        entity.update(
            request.enabled,
            request.darkLuxThreshold,
            request.brightLuxThreshold,
            request.darkDetectionTimeThreshold,
            request.brightDetectionTimeThreshold,
            request.airQualityDetectionTimeThreshold,
            request.badAirQualityRatioThreshold,
            request.cleanAirQualityRatioThreshold,
            request.cooldownMinutes,
        )

        return ControlSettingResponse.from(entity = entity)
    }

    @Transactional
    fun updateEnabled(request: UpdateControlEnabledRequest): ControlSettingResponse {
        val entity: ControlSetting = getSettingEntity()
        entity.updateEnabled(request.enabled)
        return ControlSettingResponse.from(entity = entity)
    }

    private fun getSettingEntity(): ControlSetting {
        return controlSettingRepository.findById(ControlSettingConstants.DEFAULT_ID).getOrNull()
            ?: throw InternalServerException(
                IllegalStateException("control_setting 기본 row를 찾을 수 없음.")
            )
    }

    private fun ControlSettingConstants.toEntity(): ControlSetting {
        return ControlSetting(
            DEFAULT_ID,
            DEFAULT_ENABLED,
            DEFAULT_DARK_LUX_THRESHOLD,
            DEFAULT_BRIGHT_LUX_THRESHOLD,
            DEFAULT_DARK_DETECTION_TIME_THRESHOLD,
            DEFAULT_BRIGHT_DETECTION_TIME_THRESHOLD,
            DEFAULT_AIR_QUALITY_DETECTION_TIME_THRESHOLD,
            DEFAULT_BAD_AIR_QUALITY_RATIO_THRESHOLD,
            DEFAULT_CLEAN_AIR_QUALITY_RATIO_THRESHOLD,
            DEFAULT_COOLDOWN_MINUTES,
        )
    }
}
