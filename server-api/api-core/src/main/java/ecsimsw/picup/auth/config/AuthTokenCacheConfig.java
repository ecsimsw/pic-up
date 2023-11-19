package ecsimsw.picup.auth.config;

public class AuthTokenCacheConfig {

    public static final String REDIS_HASH_KEY = "authToken";
    public static final int REDIS_TTL = 60 * 60 * 24 * 3;
}
