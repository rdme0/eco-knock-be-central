package jnu.econovation.ecoknockbecentral.light.service

import jnu.econovation.ecoknockbecentral.light.command.SaveLightReportCommand
import jnu.econovation.ecoknockbecentral.light.model.entity.LightReport
import jnu.econovation.ecoknockbecentral.light.repository.LightRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LightReportService(
    private val repository: LightRepository
) {

    @Transactional
    fun saveReport(command: SaveLightReportCommand) {
        val rawLightSensor = command.rawLightSensorDTO

        val entity = LightReport.builder()
            .rawAls(rawLightSensor.rawAls)
            .lux(rawLightSensor.lux)
            .rawWhite(rawLightSensor.rawWhite)
            .measuredAt(rawLightSensor.measuredAt)
            .build()

        repository.save(entity)
    }

}