package jnu.econovation.ecoknockbecentral.common.extension

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

fun String.isEqualConstantTime(other: String?): Boolean {
    if (other == null) {
        return false
    }

    return MessageDigest.isEqual(
        toByteArray(StandardCharsets.UTF_8),
        other.toByteArray(StandardCharsets.UTF_8),
    )
}
