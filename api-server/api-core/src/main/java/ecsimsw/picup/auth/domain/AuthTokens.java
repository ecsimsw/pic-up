package ecsimsw.picup.auth.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "authToken", timeToLive = 60 * 60 * 24 * 3)
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
