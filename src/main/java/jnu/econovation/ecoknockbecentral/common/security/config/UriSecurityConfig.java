package jnu.econovation.ecoknockbecentral.common.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "security.uri")
public record UriSecurityConfig(
        List<String> allowedFrontEndOrigins,
        List<String> allowedAdminOrigins
) {
}
