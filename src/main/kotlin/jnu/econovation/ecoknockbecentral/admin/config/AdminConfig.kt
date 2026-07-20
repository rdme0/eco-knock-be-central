package jnu.econovation.ecoknockbecentral.admin.config

import org.springframework.boot.context.properties.ConfigurationProperties
@ConfigurationProperties(prefix = "security.admin")
data class AdminConfig(
    val masterPassword: String,
    val ssoMemberId: Long,
)
