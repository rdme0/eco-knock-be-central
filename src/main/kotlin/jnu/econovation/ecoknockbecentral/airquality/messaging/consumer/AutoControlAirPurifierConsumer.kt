package jnu.econovation.ecoknockbecentral.airquality.messaging.consumer

import jakarta.annotation.PreDestroy
import jnu.econovation.ecoknockbecentral.airquality.queue.AutoControlAirPurifierQueue
import jnu.econovation.ecoknockbecentral.common.metrics.ApplicationMetrics
import jnu.econovation.ecoknockbecentral.control.service.ControlService
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class AutoControlAirPurifierConsumer(
    private val queue: AutoControlAirPurifierQueue,
    private val controlService: ControlService,
    private val metrics: ApplicationMetrics,
) : Consumer {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private var consumingJob: Job? = null

    private val consumerScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            logger.error(throwable) { "공기청정기 AutoControl consumer Scope에서 예외 포착" }
        }
    )

    @EventListener(ApplicationReadyEvent::class)
    override fun start() {
        consumingJob = consumerScope.launch { consume() }
    }

    @PreDestroy
    override fun cancel() {
        logger.info { "consuming job cancel 중" }
        consumingJob?.cancel()
    }

    override suspend fun consume() {
        queue.asFlow().collect { command ->
            runCatching {
                metrics.recordQueueProcessing("auto_control_air_purifier") {
                    controlService.autoControlAirPurifier(command = command)
                }
            }.onFailure {
                logger.error(it) { "공기청정기 auto control 실패" }
            }
        }
    }
}
