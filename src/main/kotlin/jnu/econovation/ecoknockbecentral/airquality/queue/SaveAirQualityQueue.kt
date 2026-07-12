package jnu.econovation.ecoknockbecentral.airquality.queue

import jnu.econovation.ecoknockbecentral.airquality.command.SaveAirQualityCommand
import jnu.econovation.ecoknockbecentral.common.metrics.ApplicationMetrics
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.stereotype.Component

@Component
class SaveAirQualityQueue(
    private val metrics: ApplicationMetrics,
) {

    // 항상 최신 정보를 반영하여 DB가 느려도 밀리지 않게 한다
    private val flow = MutableSharedFlow<SaveAirQualityCommand>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun enqueue(command: SaveAirQualityCommand) {
        flow.tryEmit(command)
        metrics.incrementQueueEnqueue("save_air_quality")
    }

    fun asFlow(): Flow<SaveAirQualityCommand> {
        return flow.asSharedFlow()
    }
}
