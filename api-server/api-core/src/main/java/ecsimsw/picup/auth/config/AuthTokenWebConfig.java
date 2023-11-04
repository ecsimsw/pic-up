package ecsimsw.picup.auth.config;

public class AuthTokenWebConfig {

    public static final String ACCESS_TOKEN_COOKIE_KEY = "picup-at";
    public static final String REFRESH_TOKEN_COOKIE_KEY = "picup-rt";

    public static final int ACCESS_TOKEN_COOKIE_TTL = 100;
    public static final int REFRESH_TOKEN_COOKIE_TTL = 10000;
}
