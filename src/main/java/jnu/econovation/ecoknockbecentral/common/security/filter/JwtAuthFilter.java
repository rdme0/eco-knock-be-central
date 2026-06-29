package jnu.econovation.ecoknockbecentral.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jnu.econovation.ecoknockbecentral.common.cookie.util.CookieUtil;
import jnu.econovation.ecoknockbecentral.common.security.constant.AuthPolicy;
import jnu.econovation.ecoknockbecentral.common.security.handler.Rest401Handler;
import jnu.econovation.ecoknockbecentral.common.security.handler.Rest500Handler;
import jnu.econovation.ecoknockbecentral.common.security.helper.JwtAuthHelper;
import jnu.econovation.ecoknockbecentral.common.security.resolver.AuthPolicyResolver;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

import static jnu.econovation.ecoknockbecentral.common.constant.CommonConstant.criticalError;
import static jnu.econovation.ecoknockbecentral.auth.constant.AuthConstant.ACCESS_TOKEN;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final AuthPolicyResolver policyResolver;
    private final JwtAuthHelper helper;
    private final Rest401Handler rest401Handler;
    private final Rest500Handler rest500Handler;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return policyResolver.resolve(request) == AuthPolicy.SKIP;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        AuthPolicy policy = policyResolver.resolve(request);

        try {
            Authentication auth = helper.authenticate(resolveAccessToken(request));

            if (auth instanceof AbstractAuthenticationToken token) {
                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            }

            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);

        } catch (Throwable e) {
            if (policy == AuthPolicy.OPTIONAL) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            handleException(e, request, response);
        }
    }

    private void handleException(
            Throwable e,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        SecurityContextHolder.clearContext();

        if (e instanceof AuthenticationException authenticationException) {
            CookieUtil.removeCookie(request, response, ACCESS_TOKEN);
            rest401Handler.commence(request, response, authenticationException);
            return;
        }

        AuthenticationServiceException serviceException = new AuthenticationServiceException(criticalError.apply("인증"), e);
        rest500Handler.commence(request, response, serviceException);
    }

    private String resolveAccessToken(HttpServletRequest request) {
        var cookie = WebUtils.getCookie(request, ACCESS_TOKEN);
        return cookie != null ? cookie.getValue() : null;
    }
}
