package ecsimsw.picup.env;

import javax.servlet.http.Cookie;

public class MemberFixture {

    public static final Long MEMBER_ID = 1L;

    public static final String VALID_ACCESS_TOKEN = "this is valid access token";
    public static final String VALID_REFRESH_TOKEN = "this is valid refresh token";
    public static final String INVALID_ACCESS_TOKEN = "this is invalid access token";
    public static final String INVALID_REFRESH_TOKEN = "this is invalid refresh token";

    public static final Cookie VALID_ACCESS_TOKEN_COOKIE = new Cookie("ACCESS_TOKEN", VALID_ACCESS_TOKEN);
    public static final Cookie VALID_REFRESH_TOKEN_COOKIE = new Cookie("ACCESS_TOKEN", VALID_REFRESH_TOKEN);
    public static final Cookie INVALID_ACCESS_TOKEN_COOKIE = new Cookie("REFRESH_TOKEN", INVALID_ACCESS_TOKEN);
    public static final Cookie INVALID_REFRESH_TOKEN_COOKIE = new Cookie("REFRESH_TOKEN", INVALID_REFRESH_TOKEN);
}
