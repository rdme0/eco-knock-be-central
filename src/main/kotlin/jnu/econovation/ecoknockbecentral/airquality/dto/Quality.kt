package jnu.econovation.ecoknockbecentral.airquality.dto

enum class Quality {
    VERY_BAD,
    BAD,
    NORMAL,
    GOOD,
    VERY_GOOD;

    companion object {
        fun fromPm25(pm25: Number): Quality {
            val value = pm25.toDouble()

            return when {
                value <= 7.0 -> VERY_GOOD
                value <= 15.0 -> GOOD
                value <= 35.0 -> NORMAL
                value <= 75.0 -> BAD
                else -> VERY_BAD
            }
        }

        fun fromGas(
            eco2: Double,
            bvoc: Double,
        ): Quality {
            return worseOf(fromEco2(eco2), fromBvoc(bvoc))
        }

        private fun fromEco2(eco2: Double): Quality {
            return when {
                eco2 <= 600.0 -> VERY_GOOD
                eco2 <= 1000.0 -> GOOD
                eco2 <= 1500.0 -> NORMAL
                eco2 <= 2000.0 -> BAD
                else -> VERY_BAD
            }
        }

        private fun fromBvoc(bvoc: Double): Quality {
            return when {
                bvoc <= 0.2 -> VERY_GOOD
                bvoc <= 0.5 -> GOOD
                bvoc <= 1.0 -> NORMAL
                bvoc <= 3.0 -> BAD
                else -> VERY_BAD
            }
        }

        private fun worseOf(
            first: Quality,
            second: Quality,
        ): Quality {
            return if (first.severity >= second.severity) {
                first
            } else {
                second
            }
        }
    }

    private val severity: Int
        get() = when (this) {
            VERY_GOOD -> 0
            GOOD -> 1
            NORMAL -> 2
            BAD -> 3
            VERY_BAD -> 4
        }
}
