package jnu.econovation.ecoknockbecentral.control.initializer

import jnu.econovation.ecoknockbecentral.control.repository.ControlSettingRepository
import jnu.econovation.ecoknockbecentral.control.service.ControlSettingService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
class ControlSettingInitializer(
    private val controlSettingRepository: ControlSettingRepository,
    private val controlSettingService: ControlSettingService,
) {
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener(ApplicationReadyEvent::class)
    fun initialize() {
        if (controlSettingRepository.count() == 0L) {
            controlSettingService.initializeDefaultSetting()
        }
    }
}
