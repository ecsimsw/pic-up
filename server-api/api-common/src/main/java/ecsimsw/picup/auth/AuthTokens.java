package ecsimsw.picup.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RequiredArgsConstructor
@Getter
@RedisHash(value = "AUTH_TOKEN", timeToLive = 3600)
public class AuthTokens {

    @Id
    private final String tokenKey;
    private final String accessToken;
    private final String refreshToken;
}
