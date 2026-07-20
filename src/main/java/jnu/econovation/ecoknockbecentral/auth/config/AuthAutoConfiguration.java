package jnu.econovation.ecoknockbecentral.auth.config;

import jnu.econovation.ecoknockbecentral.auth.web.resolver.PassportArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Auth 관련 자동 설정
 *
 * <p>이 설정이 포함된 서비스에서는 자동으로 {@code @PassportAuth} 어노테이션을 사용할 수 있음
 */
@Configuration
@RequiredArgsConstructor
public class AuthAutoConfiguration implements WebMvcConfigurer {

	private final PassportArgumentResolver passportArgumentResolver;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(this.passportArgumentResolver);
	}
}
