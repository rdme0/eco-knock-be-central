package jnu.econovation.ecoknockbecentral.common.security.config;

import jnu.econovation.ecoknockbecentral.common.security.filter.AdminMasterAuthFilter;
import jnu.econovation.ecoknockbecentral.common.security.filter.JwtAuthFilter;
import jnu.econovation.ecoknockbecentral.common.security.filter.SSORedirectParamFilter;
import jnu.econovation.ecoknockbecentral.common.openapi.ApiDocAccessFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String ADMIN_PATH = "/admin";
    private static final String ADMIN_PATH_PREFIX = "/admin/";
    private static final long CORS_MAX_AGE = 3600L;

    private final UriSecurityConfig uriSecurityConfig;
    private final AdminMasterAuthFilter adminMasterAuthFilter;
    private final ApiDocAccessFilter apiDocAccessFilter;
    private final JwtAuthFilter jwtAuthFilter;
    private final SSORedirectParamFilter ssoRedirectParamFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(uriSecurityConfig.allowedFrontEndOrigins());
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(CORS_MAX_AGE);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
        FilterRegistrationBean<ForwardedHeaderFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/success",
                                "/actuator/health",
                                "/actuator/info",
                                "/actuator/prometheus",
                                "/air-quality/stream",
                                "/scalar",
                                "/scalar/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/error",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/admin/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/admin/*.css", "/admin/*.js").permitAll()
                        .requestMatchers(HttpMethod.POST, "/admin/login/master").permitAll()
                        .requestMatchers(HttpMethod.POST, "/admin/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/admin/access-denied").permitAll()
                        .requestMatchers(HttpMethod.GET, "/admin", "/admin/").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/sso/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/sso/callback").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/reissue").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/test-token").permitAll()
                        .requestMatchers(HttpMethod.GET, "/air-quality/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            String uri = request.getRequestURI();
                            if (ADMIN_PATH.equals(uri) || uri.startsWith(ADMIN_PATH_PREFIX)) {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                request.getRequestDispatcher("/admin/access-denied").forward(request, response);
                                return;
                            }

                            response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        })
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(
                        ssoRedirectParamFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        adminMasterAuthFilter,
                        JwtAuthFilter.class
                )
                .addFilterBefore(
                        apiDocAccessFilter,
                        JwtAuthFilter.class
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }
}
