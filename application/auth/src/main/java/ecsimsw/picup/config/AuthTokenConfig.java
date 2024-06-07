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
public class AuthTokenConfig implements WebMvcConfigurer {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "PICUP_AT";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "PICUP_RT";

    public static final int REFRESH_TOKEN_JWT_EXPIRE_TIME_SEC = 2 * 60 * 60;
    public static final int ACCESS_TOKEN_JWT_EXPIRE_TIME_SEC = 30 * 60;

    public static final String TOKEN_PAYLOAD_NAME = "PICUP_MEMBER";

    public static Key JWT_SECRET_KEY;

    private final AuthTokenService authTokenService;

    public AuthTokenConfig(
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
            .addPathPatterns("/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthTokenArgumentResolver(authTokenService));
    }
}
