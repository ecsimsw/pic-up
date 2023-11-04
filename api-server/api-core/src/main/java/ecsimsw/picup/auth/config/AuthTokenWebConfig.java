package ecsimsw.picup.auth.config;

public class AuthTokenWebConfig {

    public static final String ACCESS_TOKEN_COOKIE_KEY = "picup-at";
    public static final String REFRESH_TOKEN_COOKIE_KEY = "picup-rt";

    public static final int ACCESS_TOKEN_COOKIE_TTL_SEC = 60 * 30;
    public static final int REFRESH_TOKEN_COOKIE_TTL_SEC = 60 * 60 * 24 * 3;
}
