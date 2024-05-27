package ecsimsw.picup.service;

import ecsimsw.picup.domain.AuthTokens;
import ecsimsw.picup.domain.AuthTokensCacheRepository;
import ecsimsw.picup.domain.TokenPayload;
import ecsimsw.picup.exception.UnauthorizedException;
import ecsimsw.picup.config.AuthTokenConfig;
import ecsimsw.picup.utils.JwtUtils;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static ecsimsw.picup.config.AuthTokenConfig.*;

@RequiredArgsConstructor
@Component
public class AuthTokenService {

    private final AuthTokensCacheRepository authTokensCacheRepository;

    public AuthTokens issue(TokenPayload payload) {
        var accessToken = createToken(payload, ACCESS_TOKEN_JWT_EXPIRE_TIME_SEC);
        var refreshToken = createToken(payload, REFRESH_TOKEN_JWT_EXPIRE_TIME_SEC);
        var authTokens = AuthTokens.of(payload, accessToken, refreshToken);
        authTokensCacheRepository.save(authTokens);
        return authTokens;
    }

    public void authenticate(HttpServletRequest request) {
        var accessToken = getAccessToken(request);
        if (!isValidToken(accessToken)) {
            throw new UnauthorizedException("Access token is not available");
        }
    }

    public AuthTokens reissue(HttpServletRequest request) {
        var refreshToken = getRefreshToken(request);
        if (!isValidToken(refreshToken)) {
            throw new UnauthorizedException("Refresh token is not available");
        }
        var tokenKey = tokenPayload(refreshToken).tokenKey();
        var currentCachedToken = authTokensCacheRepository.findById(tokenKey)
            .orElseThrow(() -> new IllegalArgumentException("Not registered refresh token"));
        var payload = tokenPayload(currentCachedToken.getRefreshToken());
        return issue(payload);
    }

    public TokenPayload tokenPayload(String token) {
        return JwtUtils.tokenValue(JWT_SECRET_KEY, token, TOKEN_PAYLOAD_NAME);
    }

    public Cookie accessTokenCookie(AuthTokens tokens) {
        var cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, tokens.getAccessToken());
        cookie.setMaxAge(ACCESS_TOKEN_JWT_EXPIRE_TIME_SEC);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public Cookie refreshTokenCookie(AuthTokens tokens) {
        var cookie = new Cookie(AuthTokenConfig.REFRESH_TOKEN_COOKIE_NAME, tokens.getAccessToken());
        cookie.setMaxAge(AuthTokenConfig.REFRESH_TOKEN_JWT_EXPIRE_TIME_SEC);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public String getAccessToken(HttpServletRequest request) {
        return getToken(request, ACCESS_TOKEN_COOKIE_NAME);
    }

    public String getRefreshToken(HttpServletRequest request) {
        return getToken(request, AuthTokenConfig.REFRESH_TOKEN_COOKIE_NAME);
    }

    private String getToken(HttpServletRequest request, String cookieName) {
        var cookies = request.getCookies();
        if (cookies == null) {
            throw new UnauthorizedException("Token is not available");
        }
        return Arrays.stream(cookies)
            .filter(cookie -> cookieName.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElseThrow(() -> new UnauthorizedException("Token is not available"));
    }

    private boolean isValidToken(String token) {
        try {
            JwtUtils.tokenValue(JWT_SECRET_KEY, token, TOKEN_PAYLOAD_NAME);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String createToken(TokenPayload payload, int expiredTime) {
        return JwtUtils.createToken(JWT_SECRET_KEY, Map.of(TOKEN_PAYLOAD_NAME, payload), expiredTime);
    }
}
