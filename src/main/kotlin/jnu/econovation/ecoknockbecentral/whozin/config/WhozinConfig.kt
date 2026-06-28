package jnu.econovation.ecoknockbecentral.whozin.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "whozin")
data class WhozinConfig(
    val baseUrl: String,
    val token: String
)