package jnu.econovation.ecoknockbecentral.airquality.command

import jnu.econovation.ecoknockbecentral.airquality.dto.grpc.RawAirPurifierDTO

data class AutoControlAirPurifierCommand(
    val isOn: Boolean
) {
    companion object {
        fun from(rawAirPurifier: RawAirPurifierDTO) : AutoControlAirPurifierCommand {
            return AutoControlAirPurifierCommand(rawAirPurifier.isOn)
        }
    }
}