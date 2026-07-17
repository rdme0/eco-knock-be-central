package jnu.econovation.ecoknockbecentral.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "security.auth-policy")
data class AuthPolicyConfig(
    val accessTokenTTL: Duration,
    val refreshTokenTTL: Duration,
    val guestSessionTTL: Duration,
    val guestLoginRateLimit: Int,
    val guestLoginRateLimitWindow: Duration,
)
