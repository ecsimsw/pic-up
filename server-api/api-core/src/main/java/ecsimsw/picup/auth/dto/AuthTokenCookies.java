package ecsimsw.picup.auth.dto;

import lombok.Getter;

import javax.servlet.http.Cookie;

@Getter
public class AuthTokenCookies {

    private final Cookie accessTokenCookie;
    private final Cookie refreshTokenCookie;

    public AuthTokenCookies(Cookie accessTokenCookie, Cookie refreshTokenCookie) {
        this.accessTokenCookie = accessTokenCookie;
        this.refreshTokenCookie = refreshTokenCookie;
    }
}
