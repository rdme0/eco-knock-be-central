package jnu.econovation.ecoknockbecentral.airquality.queue

import jnu.econovation.ecoknockbecentral.airquality.command.AutoControlAirPurifierCommand
import jnu.econovation.ecoknockbecentral.airquality.command.SaveAirQualityCommand
import jnu.econovation.ecoknockbecentral.common.metrics.ApplicationMetrics
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.stereotype.Component

@Component
class AutoControlAirPurifierQueue(
    private val metrics: ApplicationMetrics,
) {

    private val flow = MutableSharedFlow<AutoControlAirPurifierCommand>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun enqueue(command: AutoControlAirPurifierCommand) {
        flow.tryEmit(command)
        metrics.incrementQueueEnqueue("auto_control_air_purifier")
    }

    fun asFlow(): Flow<AutoControlAirPurifierCommand> {
        return flow.asSharedFlow()
    }
}
