package jnu.econovation.ecoknockbecentral.sso.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "sso")
data class SSOConfig(
    val baseUrl: String,
    val clientId: String
)