package jnu.econovation.ecoknockbecentral.sso.resolver

import jnu.econovation.ecoknockbecentral.common.security.config.UriSecurityConfig
import jnu.econovation.ecoknockbecentral.sso.exception.BadRedirectUrlException
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import java.net.URI

@Component
class SSORedirectUrlResolver(
    private val config: UriSecurityConfig,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val matcher = AntPathMatcher()

    fun resolve(rawRedirectUrl: String?): String {
        val normalizedUrl = rawRedirectUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { normalizeUrl(it) }

        if (normalizedUrl.isNullOrBlank()) {
            logger.warn { "유효하지 않은 SSO redirect url : Null or Blank" }
            throw BadRedirectUrlException()
        }

        if (!isAllowed(normalizedUrl)) {
            logger.warn { "유효하지 않은 SSO redirect url, raw -> $rawRedirectUrl, normalized -> $normalizedUrl" }
            throw BadRedirectUrlException()
        }

        return normalizedUrl
    }

    @Suppress("HttpUrlsUsage")
    private fun normalizeUrl(url: String): String {
        val trimmed = url.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }

        return if (trimmed.contains("localhost") || trimmed.contains("127.0.0.1")) {
            "http://$trimmed"
        } else {
            "https://$trimmed"
        }
    }

    private fun isAllowed(url: String?): Boolean {
        if (url.isNullOrBlank()) {
            return false
        }

        val origin = originOf(url) ?: return false

        return config.allowedFrontEndOrigins().any { pattern ->
            matcher.match(pattern.trimEnd('/'), origin)
        }
    }

    private fun originOf(url: String): String? {
        val uri = runCatching { URI(url) }.getOrNull() ?: return null
        val scheme = uri.scheme ?: return null
        val host = uri.host ?: return null

        if (scheme != "http" && scheme != "https") {
            return null
        }

        val port = if (uri.port == -1) "" else ":${uri.port}"
        return "$scheme://$host$port"
    }
}
