package jnu.econovation.ecoknockbecentral.ai.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "ai-server")
data class AIServerConfig(
    val baseUrl: String,
    val timeout: Duration,
)
