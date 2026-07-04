package jnu.econovation.ecoknockbecentral.common.extension

import jnu.econovation.ecoknockbecentral.common.util.TimeUtil
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration

fun Instant.toZonedDateTime(zoneId: ZoneId = TimeUtil.SEOUL_ZONE_ID): ZonedDateTime {
    return ZonedDateTime.ofInstant(this, zoneId)
}

operator fun Instant.minus(duration: Duration): Instant {
    return minusNanos(duration.inWholeNanoseconds)
}

operator fun Instant.plus(duration: Duration): Instant {
    return plusNanos(duration.inWholeNanoseconds)
}