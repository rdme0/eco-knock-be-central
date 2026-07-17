package jnu.econovation.ecoknockbecentral.common.security.resolver;

import jakarta.servlet.http.HttpServletRequest;
import jnu.econovation.ecoknockbecentral.common.security.constant.AuthPolicy;
import jnu.econovation.ecoknockbecentral.common.security.dto.AuthRule;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthPolicyResolver {
    private final List<AuthRule> rules = List.of(
            AuthRule.skip("/sso/login", HttpMethod.GET),
            AuthRule.skip("/sso/callback", HttpMethod.GET),
            AuthRule.skip("/auth/reissue", HttpMethod.POST),
            AuthRule.skip("/auth/test-token", HttpMethod.POST),
            AuthRule.skip("/auth/guest", HttpMethod.POST),
            AuthRule.skip("/auth/success"),
            AuthRule.skip("/admin/login", HttpMethod.GET),
            AuthRule.skip("/admin/*.css", HttpMethod.GET),
            AuthRule.skip("/admin/*.js", HttpMethod.GET),
            AuthRule.skip("/admin/login/master", HttpMethod.POST),
            AuthRule.skip("/admin/logout", HttpMethod.POST),
            AuthRule.skip("/admin/access-denied", HttpMethod.GET),
            AuthRule.skip("/actuator/health"),
            AuthRule.skip("/actuator/info"),
            AuthRule.skip("/actuator/prometheus"),
            AuthRule.skip("/air-quality/stream"),
            AuthRule.skip("/scalar"),
            AuthRule.skip("/scalar/**"),
            AuthRule.skip("/v3/api-docs"),
            AuthRule.skip("/v3/api-docs/**"),
            AuthRule.skip("/v3/api-docs.yaml"),
            AuthRule.skip("/error"),
            AuthRule.skip("/favicon.ico"),
            AuthRule.optional("/air-quality/**", HttpMethod.GET),
            AuthRule.required("/**")
//          AuthRule.optional("/posts/**", HttpMethod.GET), optional 한 인증일 때 예시
    );

    public AuthPolicy resolve(HttpServletRequest request) {
        return rules.stream()
                .filter(rule -> rule.matches(request))
                .map(AuthRule::policy)
                .findFirst()
                .orElse(AuthPolicy.REQUIRED);
    }
}
