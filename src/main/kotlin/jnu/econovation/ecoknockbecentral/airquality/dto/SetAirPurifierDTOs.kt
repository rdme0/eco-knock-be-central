package jnu.econovation.ecoknockbecentral.airquality.dto

import jnu.econovation.ecoknockbecentral.common.exception.client.BadDataSyntaxException
import java.util.Locale.getDefault

enum class Power {
    ON,
    OFF;

    companion object {
        fun from(text: String) : Power? {
            return entries.find { it.name.lowercase(locale = getDefault()) == text }
        }
    }

    fun toBoolean(): Boolean {
        return this == ON
    }
}

enum class Mode {
    AUTO,
    SILENT,
    FAVORITE,
    IDLE,
    MEDIUM,
    HIGH,
    STRONG,
    LOW;

    companion object {
        fun from(text: String) : Mode? {
            return entries.find { it.name.lowercase(locale = getDefault()) == text }
        }
    }

    override fun toString(): String {
        return name.lowercase(locale = getDefault())
    }
}

data class FavoriteLevelDTO(
    val value: Int
) {
    companion object {
        private const val MIN_LEVEL = 0
        private const val MAX_LEVEL = 17
    }
    init {
        if (value !in MIN_LEVEL..MAX_LEVEL) {
            throw BadDataSyntaxException("레벨은 $MIN_LEVEL ~ $MAX_LEVEL 사이만 가능합니다.")
        }
    }
}

