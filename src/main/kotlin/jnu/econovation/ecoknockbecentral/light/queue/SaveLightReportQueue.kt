package jnu.econovation.ecoknockbecentral.light.queue

import jnu.econovation.ecoknockbecentral.light.command.SaveLightReportCommand
import jnu.econovation.ecoknockbecentral.common.metrics.ApplicationMetrics
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.stereotype.Component

@Component
class SaveLightReportQueue(
    private val metrics: ApplicationMetrics,
) {

    // 항상 최신 정보를 반영하여 DB가 느려도 밀리지 않게 한다
    private val flow = MutableSharedFlow<SaveLightReportCommand>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun enqueue(command: SaveLightReportCommand) {
        flow.tryEmit(command)
        metrics.incrementQueueEnqueue("save_light_report")
    }

    fun asFlow(): Flow<SaveLightReportCommand> {
        return flow.asSharedFlow()
    }
}
