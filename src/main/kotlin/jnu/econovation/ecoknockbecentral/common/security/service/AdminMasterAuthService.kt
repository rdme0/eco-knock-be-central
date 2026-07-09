package jnu.econovation.ecoknockbecentral.common.security.service

import jnu.econovation.ecoknockbecentral.common.extension.isEqualConstantTime
import jnu.econovation.ecoknockbecentral.common.security.config.AdminSecurityConfig
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil
import org.springframework.stereotype.Service

@Service
class AdminMasterAuthService(
    private val adminSecurityConfig: AdminSecurityConfig,
    private val jwtUtil: JwtUtil,
) {
    fun authenticate(password: String?): AdminMasterAuthToken? {
        if (!adminSecurityConfig.masterPassword.isEqualConstantTime(password)) {
            return null
        }

        val ttl = adminSecurityConfig.masterTokenTTL
        return AdminMasterAuthToken(
            value = jwtUtil.generateAdminMasterToken(ttl),
            maxAgeSeconds = ttl.toSeconds().toInt(),
        )
    }
}

data class AdminMasterAuthToken(
    val value: String,
    val maxAgeSeconds: Int,
)
