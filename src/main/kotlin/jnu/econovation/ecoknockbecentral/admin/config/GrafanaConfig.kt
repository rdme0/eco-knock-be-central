package jnu.econovation.ecoknockbecentral.admin.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "grafana")
data class GrafanaConfig(
    val url: String,
)
