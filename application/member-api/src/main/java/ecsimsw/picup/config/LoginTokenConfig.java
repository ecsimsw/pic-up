package ecsimsw.picup.config;

import ecsimsw.picup.service.AuthTokenArgumentResolver;
import ecsimsw.picup.service.AuthTokenInterceptor;
import ecsimsw.picup.service.AuthTokenService;
import ecsimsw.picup.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.security.Key;
import java.util.List;

@Configuration
public class LoginTokenConfig implements WebMvcConfigurer {

    public static Key JWT_SECRET_KEY;

    private final AuthTokenService authTokenService;

    public LoginTokenConfig(
        AuthTokenService authTokenService,
        @Value("${picup.token.secret.key}")
        String tokenSecretKey
    ) {
        this.authTokenService = authTokenService;
        JWT_SECRET_KEY = JwtUtils.createSecretKey(tokenSecretKey);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthTokenInterceptor(authTokenService))
            .addPathPatterns("/**")
            .excludePathPatterns();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthTokenArgumentResolver(authTokenService));
    }
}
