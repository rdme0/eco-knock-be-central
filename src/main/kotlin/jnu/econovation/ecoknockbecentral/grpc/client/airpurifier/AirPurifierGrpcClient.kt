package jnu.econovation.ecoknockbecentral.grpc.client.airpurifier

import jnu.econovation.ecoknockbecentral.airquality.dto.grpc.request.FavoriteLevelDTO
import jnu.econovation.ecoknockbecentral.airquality.dto.grpc.request.Mode
import jnu.econovation.ecoknockbecentral.airquality.dto.grpc.request.Power
import jnu.econovation.ecoknockbecentral.airquality.dto.grpc.RawAirPurifierDTO
import jnu.econovation.ecoknockbecentral.common.metrics.ApplicationMetrics
import jnu.econovation.ecoknockbecentral.grpc.airpurifier.v1.AirPurifierServiceGrpc
import jnu.econovation.ecoknockbecentral.grpc.airpurifier.v1.GetCurrentAirPurifierRequest
import jnu.econovation.ecoknockbecentral.grpc.airpurifier.v1.SetAirPurifierFavoriteLevelRequest
import jnu.econovation.ecoknockbecentral.grpc.airpurifier.v1.SetAirPurifierModeRequest
import jnu.econovation.ecoknockbecentral.grpc.airpurifier.v1.SetAirPurifierPowerRequest
import jnu.econovation.ecoknockbecentral.grpc.config.EmbeddedGrpcConfig
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class AirPurifierGrpcClient(
    private val config: EmbeddedGrpcConfig,
    private val stub: AirPurifierServiceGrpc.AirPurifierServiceBlockingStub,
    private val metrics: ApplicationMetrics,
) {
    fun getCurrentAirPurifier(): RawAirPurifierDTO {
        val response = metrics.recordGrpcClient("air_purifier", "get_current") {
            stub
                .withDeadlineAfter(config.timeout.toMillis(), TimeUnit.MILLISECONDS)
                .getCurrentAirPurifier(GetCurrentAirPurifierRequest.getDefaultInstance())
        }

        return RawAirPurifierDTO.from(response)
    }

    fun setAirPurifierPower(power: Power): RawAirPurifierDTO {
        val request = SetAirPurifierPowerRequest.newBuilder()
            .setOn(power.toBoolean())
            .build()

        val response = metrics.recordGrpcClient("air_purifier", "set_power") {
            stub
                .withDeadlineAfter(config.timeout.toMillis(), TimeUnit.MILLISECONDS)
                .setAirPurifierPower(request)
        }

        return RawAirPurifierDTO.from(response.current)
    }

    fun setAirPurifierMode(mode: Mode): RawAirPurifierDTO {
        val request = SetAirPurifierModeRequest.newBuilder()
            .setMode(mode.toString())
            .build()

        val response = metrics.recordGrpcClient("air_purifier", "set_mode") {
            stub
                .withDeadlineAfter(config.timeout.toMillis(), TimeUnit.MILLISECONDS)
                .setAirPurifierMode(request)
        }

        return RawAirPurifierDTO.from(response.current)
    }

    fun setAirPurifierFavoriteLevel(level: FavoriteLevelDTO): RawAirPurifierDTO {
        val request = SetAirPurifierFavoriteLevelRequest.newBuilder()
            .setLevel(level.value)
            .build()

        val response = metrics.recordGrpcClient("air_purifier", "set_favorite_level") {
            stub
                .withDeadlineAfter(config.timeout.toMillis(), TimeUnit.MILLISECONDS)
                .setAirPurifierFavoriteLevel(request)
        }

        return RawAirPurifierDTO.from(response.current)
    }
}
