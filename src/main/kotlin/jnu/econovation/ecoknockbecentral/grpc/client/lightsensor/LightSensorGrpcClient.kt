package jnu.econovation.ecoknockbecentral.grpc.client.lightsensor

import jnu.econovation.ecoknockbecentral.grpc.config.EmbeddedGrpcConfig
import jnu.econovation.ecoknockbecentral.grpc.lightsensor.v1.GetCurrentLightSensorRequest
import jnu.econovation.ecoknockbecentral.grpc.lightsensor.v1.LightSensorServiceGrpc
import jnu.econovation.ecoknockbecentral.light.dto.RawLightSensorDTO
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.TimeUnit

@Component
class LightSensorGrpcClient(
    private val config: EmbeddedGrpcConfig,
    private val stub: LightSensorServiceGrpc.LightSensorServiceBlockingStub,
) {
    suspend fun getCurrentLightSensor(): RawLightSensorDTO {
        val response = stub
            .withDeadlineAfter(config.timeout.toMillis(), TimeUnit.MILLISECONDS)
            .getCurrentLightSensor(GetCurrentLightSensorRequest.getDefaultInstance())

        return RawLightSensorDTO(
            lux = response.lux,
            measuredAt = Instant.ofEpochMilli(response.measuredAtUnixMs),
            rawAls = response.rawAls,
            rawWhite = response.rawWhite
        )
    }
}
