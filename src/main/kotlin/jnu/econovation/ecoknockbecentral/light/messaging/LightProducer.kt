package jnu.econovation.ecoknockbecentral.light.messaging

import jakarta.annotation.PreDestroy
import jnu.econovation.ecoknockbecentral.grpc.client.lightsensor.LightSensorGrpcClient
import jnu.econovation.ecoknockbecentral.light.command.SaveLightReportCommand
import jnu.econovation.ecoknockbecentral.light.dto.RawLightSensorDTO
import jnu.econovation.ecoknockbecentral.light.queue.SaveLightReportQueue
import jnu.econovation.ecoknockbecentral.common.metrics.ApplicationMetrics
import kotlinx.coroutines.*
import kotlinx.coroutines.time.delay
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class LightProducer(
    private val lightSensorGrpcClient: LightSensorGrpcClient,
    private val queue: SaveLightReportQueue,
    private val metrics: ApplicationMetrics,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val POLLING_DELAY = Duration.ofSeconds(1)
        private val INITIAL_ERROR_DELAY = Duration.ofSeconds(30)
    }

    private val producerScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            logger.error(throwable) { "gRPC 센서 producer Scope에서 예외 포착" }
        }
    )

    private var producingJob: Job? = null
    private var errorDelay = INITIAL_ERROR_DELAY


    @EventListener(ApplicationReadyEvent::class)
    fun start() {
        producingJob = producerScope.launch {
            pollAndProduce()
        }
    }

    @PreDestroy
    fun cancel() {
        logger.info { "producing job cancel 중" }
        producingJob?.cancel()
    }


    private suspend fun pollAndProduce() = supervisorScope {
        while (true) {
            val pollingSample = metrics.startTimer()
            val lightSensor: RawLightSensorDTO = runCatching {
                lightSensorGrpcClient.getCurrentLightSensor()
            }.onFailure {
                logger.error(it) { "grpc light sensor polling 에러" }
            }.getOrNull()
                ?: run {
                    metrics.stopPollingCycle(pollingSample, "light", "failure")
                    logger.warn { "gRPC 에러로 인해 light sensor polling ${errorDelay.toSeconds()}초 딜레이" }
                    delay(errorDelay)
                    errorDelay = errorDelay.multipliedBy(2)

                    continue
                }

            logger.debug { "light sensor gRPC -> $lightSensor" }

            val command = SaveLightReportCommand(lightSensor)

            queue.enqueue(command)

            metrics.stopPollingCycle(pollingSample, "light", "success")
            errorDelay = INITIAL_ERROR_DELAY

            delay(POLLING_DELAY)
        }
    }
}
