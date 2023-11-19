package ecsimsw.picup.env;

import javax.servlet.http.Cookie;

import static ecsimsw.picup.auth.config.AuthTokenWebConfig.ACCESS_TOKEN_COOKIE_KEY;
import static ecsimsw.picup.auth.config.AuthTokenWebConfig.REFRESH_TOKEN_COOKIE_KEY;

public class MemberFixture {

    public static final Long MEMBER_ID = 1L;
    public static final String MEMBER_USERNAME = "ecsimsw";

    public static final String VALID_ACCESS_TOKEN = "this is valid access token";
    public static final String VALID_REFRESH_TOKEN = "this is valid refresh token";
    public static final String INVALID_ACCESS_TOKEN = "this is invalid access token";
    public static final String INVALID_REFRESH_TOKEN = "this is invalid refresh token";

    public static final Cookie VALID_ACCESS_TOKEN_COOKIE = new Cookie(ACCESS_TOKEN_COOKIE_KEY, VALID_ACCESS_TOKEN);
    public static final Cookie VALID_REFRESH_TOKEN_COOKIE = new Cookie(REFRESH_TOKEN_COOKIE_KEY, VALID_REFRESH_TOKEN);
    public static final Cookie INVALID_ACCESS_TOKEN_COOKIE = new Cookie(ACCESS_TOKEN_COOKIE_KEY, INVALID_ACCESS_TOKEN);
    public static final Cookie INVALID_REFRESH_TOKEN_COOKIE = new Cookie(REFRESH_TOKEN_COOKIE_KEY, INVALID_REFRESH_TOKEN);
}
