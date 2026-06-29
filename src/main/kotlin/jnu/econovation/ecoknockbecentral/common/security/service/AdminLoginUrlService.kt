package jnu.econovation.ecoknockbecentral.common.security.service

import jakarta.servlet.http.HttpServletRequest
import jnu.econovation.ecoknockbecentral.sso.resolver.SSORedirectUrlResolver
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder

@Service
class AdminLoginUrlService(
    private val redirectUrlResolver: SSORedirectUrlResolver,
) {
    companion object {
        const val ADMIN_HOME_PATH = "/admin"
    }

    fun ssoLoginUrl(request: HttpServletRequest): String? {
        val redirectUrl = adminHomeUrl(request)

        return runCatching {
            redirectUrlResolver.resolveAdmin(redirectUrl)
            UriComponentsBuilder.fromPath("/sso/login")
                .queryParam("redirect", redirectUrl)
                .build()
                .toUriString()
        }.getOrNull()
    }

    private fun adminHomeUrl(request: HttpServletRequest): String {
        val port = request.serverPort
        val origin = UriComponentsBuilder.newInstance()
            .scheme(request.scheme)
            .host(request.serverName)
            .apply {
                if (!isDefaultPort(request.scheme, port)) {
                    port(port)
                }
            }
            .build()
            .toUriString()

        return "$origin$ADMIN_HOME_PATH"
    }

    private fun isDefaultPort(scheme: String, port: Int): Boolean {
        return (scheme == "http" && port == 80) || (scheme == "https" && port == 443)
    }
}
