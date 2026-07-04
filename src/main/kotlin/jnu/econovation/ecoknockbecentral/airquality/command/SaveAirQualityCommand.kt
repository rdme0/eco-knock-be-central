package jnu.econovation.ecoknockbecentral.airquality.command

import jnu.econovation.ecoknockbecentral.airquality.dto.internal.AirQualityDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.grpc.RawAirPurifierDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.grpc.RawSensorDTO

data class SaveAirQualityCommand(
    val airQuality: AirQualityDTO,
    val rawSensor: RawSensorDTO,
    val rawAirPurifier: RawAirPurifierDTO
)