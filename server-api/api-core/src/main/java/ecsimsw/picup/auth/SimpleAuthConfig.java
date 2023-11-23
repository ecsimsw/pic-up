package ecsimsw.picup.auth;

import ecsimsw.auth.anotations.EnableSimpleAuth;
import ecsimsw.auth.domain.AuthTokensCacheRepository;
import ecsimsw.auth.domain.TokenCookieHolder;
import ecsimsw.auth.service.AuthTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableSimpleAuth
@Configuration
public class SimpleAuthConfig implements WebMvcConfigurer {

    private static final String ACCESS_TOKEN_COOKIE_NAME  = "PICUP_AT";
    private static final String REFRESH_TOKEN_COOKIE_NAME  = "PICUP_RT";

    private final int accessTokenTTL;
    private final int refreshTokenTTL;

    public SimpleAuthConfig(
        @Value("ecsimsw.access.token.ttl.sec") int accessTokenTTL,
        @Value("ecsimsw.refresh.token.ttl.sec") int refreshTokenTTL
    ) {
        this.accessTokenTTL = accessTokenTTL;
        this.refreshTokenTTL = refreshTokenTTL;
    }

    @Bean
    public AuthTokenService<AuthTokenPayload> authTokenService(
        @Value("${ecsimsw.token.secret.key}") String jwtSecretKey,
        @Autowired AuthTokensCacheRepository authTokensCacheRepository
    ) {
        var atCookieBuilder = TokenCookieHolder.from(ACCESS_TOKEN_COOKIE_NAME, accessTokenTTL).secure(false).build();
        var rtCookieBuilder = TokenCookieHolder.from(REFRESH_TOKEN_COOKIE_NAME, refreshTokenTTL).secure(false).build();
        return new AuthTokenService<>(
            jwtSecretKey,
            authTokensCacheRepository,
            atCookieBuilder,
            rtCookieBuilder,
            AuthTokenPayload.class
        );
    }
}
