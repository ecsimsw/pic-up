package ecsimsw.picup.auth.service;

import ecsimsw.picup.auth.domain.AuthTokens;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.List;

import static ecsimsw.picup.auth.config.AuthTokenWebConfig.*;

public class TokenCookieUtils {

    public static String getTokenFromCookies(Cookie[] cookies, String tokenCookieKey) {
        if (cookies == null) {
            throw new IllegalArgumentException("Not authorized - No cookie");
        }
        return Arrays.stream(cookies)
            .filter(cookie -> tokenCookieKey.equals(cookie.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Not authorized"))
            .getValue();
    }

    public static List<Cookie> createAuthCookies(AuthTokens authTokens) {
        final Cookie accessTokenCookie = createAccessTokenCookie(authTokens.getAccessToken());
        final Cookie refreshTokenCookie = createRefreshTokenCookie(authTokens.getRefreshToken());
        return List.of(accessTokenCookie, refreshTokenCookie);
    }

    public static Cookie createAccessTokenCookie(String accessToken) {
        final Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_KEY, accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(ACCESS_TOKEN_COOKIE_TTL_SEC);
        cookie.setSecure(false);
        return cookie;
    }

    public static Cookie createRefreshTokenCookie(String refreshToken) {
        final Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_KEY, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(REFRESH_TOKEN_COOKIE_TTL_SEC);
        cookie.setSecure(false);
        return cookie;
    }
}
