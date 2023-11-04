package ecsimsw.picup.auth.config;

public class AuthTokenJwtConfig {

    public static final int ACCESS_TOKEN_JWT_EXPIRE_TIME = 100;
    public static final int REFRESH_TOKEN_JWT_EXPIRE_TIME = 1000;

    public static final String TOKEN_JWT_PAYLOAD_KEY = "member";
}
