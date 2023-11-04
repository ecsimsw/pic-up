package ecsimsw.picup.auth.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import static ecsimsw.picup.auth.config.AuthTokenCacheConfig.REDIS_HASH_KEY;
import static ecsimsw.picup.auth.config.AuthTokenCacheConfig.REDIS_TTL;

@Getter
@RedisHash(value = REDIS_HASH_KEY, timeToLive = REDIS_TTL)
public class AuthTokens {

    @Id
    private final String username;
    private final String accessToken;
    private final String refreshToken;

    public AuthTokens(String username, String accessToken, String refreshToken) {
        this.username = username;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public void checkSameWith(String accessToken, String refreshToken) {
        if(this.accessToken.equals(accessToken) && this.refreshToken.equals(refreshToken)) {
            return;
        }
        throw new IllegalArgumentException("Not sync with cached auth tokens");
    }
}
