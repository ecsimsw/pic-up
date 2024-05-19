package ecsimsw.picup.auth;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static ecsimsw.picup.auth.AuthTokenConfig.*;

@RequiredArgsConstructor
@Component
public class AuthTokenService {

    private final AuthTokensCacheRepository authTokensCacheRepository;

    public AuthTokens issue(LoginUser payload) {
        var accessToken = createToken(payload, ACCESS_TOKEN_JWT_EXPIRE_TIME);
        var refreshToken = createToken(payload, REFRESH_TOKEN_JWT_EXPIRE_TIME);
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
        var currentCachedToken = authTokensCacheRepository.findById(refreshToken)
            .orElseThrow(() -> new IllegalArgumentException("Not registered refresh token"));
        var payload = tokenPayload(currentCachedToken.getRefreshToken());
        return issue(payload);
    }

    public LoginUser tokenPayload(String token) {
        return JwtUtils.tokenValue(JWT_SECRET_KEY, token, TOKEN_PAYLOAD_NAME);
    }

    public Cookie accessTokenCookie(AuthTokens tokens) {
        var cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, tokens.getAccessToken());
        cookie.setMaxAge(ACCESS_TOKEN_JWT_EXPIRE_TIME);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public Cookie refreshTokenCookie(AuthTokens tokens) {
        var cookie = new Cookie(AuthTokenConfig.REFRESH_TOKEN_COOKIE_NAME, tokens.getAccessToken());
        cookie.setMaxAge(AuthTokenConfig.REFRESH_TOKEN_JWT_EXPIRE_TIME);
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

    private String createToken(LoginUser payload, int expiredTime) {
        return JwtUtils.createToken(JWT_SECRET_KEY, Map.of(TOKEN_PAYLOAD_NAME, payload), expiredTime);
    }
}
