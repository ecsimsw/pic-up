package ecsimsw.picup.domain;

import static ecsimsw.picup.config.AuthTokenConfig.REFRESH_TOKEN_JWT_EXPIRE_TIME_SEC;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RequiredArgsConstructor
@Getter
@RedisHash(value = "AUTH_TOKEN", timeToLive = REFRESH_TOKEN_JWT_EXPIRE_TIME_SEC)
public class AuthTokens {

    @Id
    private final String tokenKey;
    private final String accessToken;
    private final String refreshToken;

    public static AuthTokens of(TokenPayload payload, String accessToken, String refreshToken) {
        return new AuthTokens(
            payload.tokenKey(),
            accessToken,
            refreshToken
        );
    }

    public void checkRefreshToken(String refreshToken) {
        if(!this.refreshToken.equals(refreshToken)) {
            throw new IllegalArgumentException("Not registered refresh token");
        }
    }
}
