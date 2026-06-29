package jnu.econovation.ecoknockbecentral.common.security.service

import jnu.econovation.ecoknockbecentral.common.security.config.AdminSecurityConfig
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@Service
class AdminMasterAuthService(
    private val adminSecurityConfig: AdminSecurityConfig,
    private val jwtUtil: JwtUtil,
) {
    fun authenticate(password: String?): AdminMasterAuthToken? {
        if (!matchesMasterPassword(password)) {
            return null
        }

        val ttl = adminSecurityConfig.masterTokenTTL
        return AdminMasterAuthToken(
            value = jwtUtil.generateAdminMasterToken(ttl),
            maxAgeSeconds = ttl.toSeconds().toInt(),
        )
    }

    private fun matchesMasterPassword(password: String?): Boolean {
        if (password == null) {
            return false
        }

        return MessageDigest.isEqual(
            adminSecurityConfig.masterPassword.toByteArray(StandardCharsets.UTF_8),
            password.toByteArray(StandardCharsets.UTF_8),
        )
    }
}

data class AdminMasterAuthToken(
    val value: String,
    val maxAgeSeconds: Int,
)
