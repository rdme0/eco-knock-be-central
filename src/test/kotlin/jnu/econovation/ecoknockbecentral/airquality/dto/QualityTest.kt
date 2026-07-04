package jnu.econovation.ecoknockbecentral.airquality.dto

import jnu.econovation.ecoknockbecentral.airquality.dto.internal.Quality
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class QualityTest {
    @Test
    fun `PM25 경계값으로 공기질을 분류한다`() {
        assertThat(Quality.fromPm25(7)).isEqualTo(Quality.VERY_GOOD)
        assertThat(Quality.fromPm25(8)).isEqualTo(Quality.GOOD)
        assertThat(Quality.fromPm25(15)).isEqualTo(Quality.GOOD)
        assertThat(Quality.fromPm25(16)).isEqualTo(Quality.NORMAL)
        assertThat(Quality.fromPm25(35)).isEqualTo(Quality.NORMAL)
        assertThat(Quality.fromPm25(36)).isEqualTo(Quality.BAD)
        assertThat(Quality.fromPm25(75)).isEqualTo(Quality.BAD)
        assertThat(Quality.fromPm25(76)).isEqualTo(Quality.VERY_BAD)
    }

    @Test
    fun `eCO2와 BVOC 중 더 나쁜 등급을 가스 공기질로 사용한다`() {
        assertThat(Quality.fromGas(eco2 = 600.0, bvoc = 0.2)).isEqualTo(Quality.VERY_GOOD)
        assertThat(Quality.fromGas(eco2 = 1000.0, bvoc = 0.5)).isEqualTo(Quality.GOOD)
        assertThat(Quality.fromGas(eco2 = 1500.0, bvoc = 1.0)).isEqualTo(Quality.NORMAL)
        assertThat(Quality.fromGas(eco2 = 2000.0, bvoc = 3.0)).isEqualTo(Quality.BAD)
        assertThat(Quality.fromGas(eco2 = 2000.1, bvoc = 3.1)).isEqualTo(Quality.VERY_BAD)
        assertThat(Quality.fromGas(eco2 = 500.0, bvoc = 3.1)).isEqualTo(Quality.VERY_BAD)
        assertThat(Quality.fromGas(eco2 = 2000.1, bvoc = 0.1)).isEqualTo(Quality.VERY_BAD)
    }
}
