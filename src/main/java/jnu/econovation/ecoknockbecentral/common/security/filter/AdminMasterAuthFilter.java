package jnu.econovation.ecoknockbecentral.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jnu.econovation.ecoknockbecentral.common.cookie.util.CookieUtil;
import jnu.econovation.ecoknockbecentral.common.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.List;

import static jnu.econovation.ecoknockbecentral.common.security.constant.AdminAuthConstant.ADMIN_MASTER_TOKEN;

@Component
@RequiredArgsConstructor
public class AdminMasterAuthFilter extends OncePerRequestFilter {
    private static final String ADMIN_PATH = "/admin";
    private static final String ADMIN_PATH_PREFIX = "/admin/";
    private static final String ADMIN_USERNAME = "admin-master";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private final JwtUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !isAdminPageRequest(request)
                || SecurityContextHolder.getContext().getAuthentication() != null;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveAdminMasterToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtUtil.validateAdminMasterToken(token)) {
            CookieUtil.removeCookie(request, response, ADMIN_MASTER_TOKEN);
            filterChain.doFilter(request, response);
            return;
        }

        var authentication = new UsernamePasswordAuthenticationToken(
                ADMIN_USERNAME,
                null,
                List.of(new SimpleGrantedAuthority(ADMIN_ROLE))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private String resolveAdminMasterToken(HttpServletRequest request) {
        var cookie = WebUtils.getCookie(request, ADMIN_MASTER_TOKEN);
        return cookie != null ? cookie.getValue() : null;
    }

    private boolean isAdminPageRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return ADMIN_PATH.equals(uri) || uri.startsWith(ADMIN_PATH_PREFIX);
    }
}
