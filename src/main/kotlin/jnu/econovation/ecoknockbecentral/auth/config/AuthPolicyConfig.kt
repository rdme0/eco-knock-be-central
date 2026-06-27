package jnu.econovation.ecoknockbecentral.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "security.auth-policy")
data class AuthPolicyConfig(
    private val accessTokenTTL: Duration,
    private val refreshTokenTTL: Duration,
) {
    fun accessTokenTTL(): Duration = accessTokenTTL

    fun refreshTokenTTL(): Duration = refreshTokenTTL
}
