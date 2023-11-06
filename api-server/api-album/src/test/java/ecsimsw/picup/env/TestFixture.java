package ecsimsw.picup.env;

import static ecsimsw.picup.auth.config.AuthTokenWebConfig.ACCESS_TOKEN_COOKIE_KEY;
import static ecsimsw.picup.auth.config.AuthTokenWebConfig.REFRESH_TOKEN_COOKIE_KEY;

import javax.servlet.http.Cookie;

public class TestFixture {

    public static final Long MEMBER_ID = 1L;
    public static final String MEMBER_USERNAME = "ecsimsw";
    public static final Long ALBUM_ID = 1L;
    public static final String ALBUM_NAME = "album name";
    public static final String THUMBNAIL_RESOURCE_KEY = "this is thumbnail resource key";
    public static final String RESOURCE_KEY = "this is resource key";
    public static final String DESCRIPTION = "this is description of picture";

    public static final String VALID_ACCESS_TOKEN = "this is valid access token";
    public static final String VALID_REFRESH_TOKEN = "this is valid refresh token";
    public static final String INVALID_ACCESS_TOKEN = "this is invalid access token";
    public static final String INVALID_REFRESH_TOKEN = "this is invalid refresh token";

    public static final Cookie VALID_ACCESS_TOKEN_COOKIE = new Cookie(ACCESS_TOKEN_COOKIE_KEY, VALID_ACCESS_TOKEN);
    public static final Cookie VALID_REFRESH_TOKEN_COOKIE = new Cookie(REFRESH_TOKEN_COOKIE_KEY, VALID_REFRESH_TOKEN);
    public static final Cookie INVALID_ACCESS_TOKEN_COOKIE = new Cookie(ACCESS_TOKEN_COOKIE_KEY, INVALID_ACCESS_TOKEN);
    public static final Cookie INVALID_REFRESH_TOKEN_COOKIE = new Cookie(REFRESH_TOKEN_COOKIE_KEY, INVALID_REFRESH_TOKEN);
}
