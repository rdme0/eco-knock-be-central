package jnu.econovation.ecoknockbecentral.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "security.test-auth")
data class TestAuthConfig(
    val ssoMemberId: Long,
)
