package jnu.econovation.ecoknockbecentral.light.command

import jnu.econovation.ecoknockbecentral.light.dto.RawLightSensorDTO

data class SaveLightReportCommand(
    val rawLightSensorDTO: RawLightSensorDTO
)