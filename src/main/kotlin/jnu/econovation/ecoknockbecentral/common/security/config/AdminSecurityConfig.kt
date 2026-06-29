package jnu.econovation.ecoknockbecentral.common.security.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "security.admin")
data class AdminSecurityConfig(
    val masterPassword: String,
    val masterTokenTTL: Duration,
)
