package jnu.econovation.ecoknockbecentral.common.openapi

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class ApiDocAccessFilter(
    private val apiDocAccessService: ApiDocAccessService,
) : OncePerRequestFilter() {
    companion object {
        private const val SCALAR_PATH = "/scalar"
        private const val SCALAR_PATH_PREFIX = "/scalar/"
        private const val API_DOCS_PATH = "/v3/api-docs"
        private const val API_DOCS_PATH_PREFIX = "/v3/api-docs/"
        private const val API_DOCS_YAML_PATH = "/v3/api-docs.yaml"
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return !isApiDocRequest(request.requestURI)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (!apiDocAccessService.isEnabled()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun isApiDocRequest(uri: String): Boolean {
        return uri == SCALAR_PATH ||
            uri.startsWith(SCALAR_PATH_PREFIX) ||
            uri == API_DOCS_PATH ||
            uri.startsWith(API_DOCS_PATH_PREFIX) ||
            uri == API_DOCS_YAML_PATH
    }
}
