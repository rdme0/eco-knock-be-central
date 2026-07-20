package jnu.econovation.ecoknockbecentral.sso.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "sso")
data class SSOConfig(
    val loginPageBaseUrl: String,
    val gatewayPassportUrl: String,
    val clientId: String
)
