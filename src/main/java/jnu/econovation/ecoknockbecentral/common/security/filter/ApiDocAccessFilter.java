package jnu.econovation.ecoknockbecentral.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jnu.econovation.ecoknockbecentral.common.openapi.service.ApiDocAccessService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ApiDocAccessFilter extends OncePerRequestFilter {
    private static final String SCALAR_PATH = "/scalar";
    private static final String SCALAR_PATH_PREFIX = "/scalar/";
    private static final String API_DOCS_PATH = "/v3/api-docs";
    private static final String API_DOCS_PATH_PREFIX = "/v3/api-docs/";
    private static final String API_DOCS_YAML_PATH = "/v3/api-docs.yaml";

    private final ApiDocAccessService apiDocAccessService;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !isApiDocRequest(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!apiDocAccessService.isEnabled()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isApiDocRequest(String uri) {
        return uri.equals(SCALAR_PATH)
                || uri.startsWith(SCALAR_PATH_PREFIX)
                || uri.equals(API_DOCS_PATH)
                || uri.startsWith(API_DOCS_PATH_PREFIX)
                || uri.equals(API_DOCS_YAML_PATH);
    }
}
