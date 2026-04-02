package jnu.econovation.ecoknockbecentral.grpc.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "grpc.server")
data class GrpcConfig(
    val port: Int,
    val testPort: Int
)