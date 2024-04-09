package ecsimsw.picup.auth;

import java.security.Key;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class AuthConfig implements WebMvcConfigurer {

    public static final Key JWT_SECRET_KEY = JwtUtils.createSecretKey("thisissecretkeythisissecretkeythisis");

    public static final String ACCESS_TOKEN_COOKIE_NAME = "PICUP_AT";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "PICUP_RT";

    public static final int REFRESH_TOKEN_JWT_EXPIRE_TIME = 2 * 60 * 60;
    public static final int ACCESS_TOKEN_JWT_EXPIRE_TIME = 30 * 60;

    private final AuthInterceptor loginUserInfoAuthInterceptor;
    private final AuthArgumentResolver authArgumentResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInfoAuthInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authArgumentResolver);
    }
}
