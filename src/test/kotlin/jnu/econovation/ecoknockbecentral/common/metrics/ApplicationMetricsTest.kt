package jnu.econovation.ecoknockbecentral.common.metrics

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ApplicationMetricsTest {

    private val registry = SimpleMeterRegistry()
    private val metrics = ApplicationMetrics(registry)

    @Test
    fun `gRPC metric은 성공과 실패 결과를 제한된 태그로 기록한다`() {
        metrics.recordGrpcClient("sensor", "get_current") { "ok" }

        assertThatThrownBy {
            metrics.recordGrpcClient("sensor", "get_current") { error("failed") }
        }.isInstanceOf(IllegalStateException::class.java)

        assertThat(registry.find("eco.knock.grpc.client").tags("client", "sensor", "operation", "get_current", "outcome", "success").timer()?.count())
            .isEqualTo(1)
        assertThat(registry.find("eco.knock.grpc.client").tags("client", "sensor", "operation", "get_current", "outcome", "failure").timer()?.count())
            .isEqualTo(1)
    }

    @Test
    fun `queue enqueue metric은 queue 이름만 태그로 사용한다`() {
        metrics.incrementQueueEnqueue("save_air_quality")

        assertThat(registry.find("eco.knock.queue.enqueue").tags("queue", "save_air_quality").counter()?.count())
            .isEqualTo(1.0)
    }
}
