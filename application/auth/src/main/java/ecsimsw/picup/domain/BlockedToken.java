package ecsimsw.picup.domain;

import static ecsimsw.picup.config.AuthTokenConfig.REFRESH_TOKEN_JWT_EXPIRE_TIME_SEC;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RequiredArgsConstructor
@Getter
@RedisHash(value = "BLOCKING_TOKEN", timeToLive = REFRESH_TOKEN_JWT_EXPIRE_TIME_SEC)
public class BlockedToken {

    @Id
    private final String token;

    public static BlockedToken of(String token) {
        return new BlockedToken(token);
    }
}
