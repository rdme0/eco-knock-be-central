package jnu.econovation.ecoknockbecentral.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jnu.econovation.ecoknockbecentral.common.cookie.util.CookieUtil;
import jnu.econovation.ecoknockbecentral.common.security.handler.Rest400Handler;
import jnu.econovation.ecoknockbecentral.sso.exception.BadRedirectUrlException;
import jnu.econovation.ecoknockbecentral.sso.resolver.SSORedirectUrlResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static jnu.econovation.ecoknockbecentral.sso.constant.SSOConstant.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SSORedirectParamFilter extends OncePerRequestFilter {
    private final SSORedirectUrlResolver redirectUrlResolver;
    private final Rest400Handler rest400Handler;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !HttpMethod.GET.name().equals(request.getMethod())
                || !SSO_LOGIN_PATH.equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String rawRedirectUrl = request.getParameter(REDIRECT_PARAM);

        try {
            String redirectUrl = redirectUrlResolver.resolve(rawRedirectUrl);

            CookieUtil.addCookie(
                    request,
                    response,
                    SSO_REDIRECT_URL_KEY,
                    redirectUrl,
                    SSO_REDIRECT_COOKIE_MAX_AGE
            );

            filterChain.doFilter(request, response);
        } catch (BadRedirectUrlException exception) {
            log.warn("유효하지 않은 SSO redirect url, raw -> {}", rawRedirectUrl);
            rest400Handler.commence(request, response, exception);
        }
    }
}
