package ecsimsw.picup.auth;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import static ecsimsw.picup.auth.AuthConfig.*;

@RequiredArgsConstructor
@Component
public class AuthTokenService {

    private static final String TOKEN_PAYLOAD_NAME = "picup_user";

    private final AuthTokensCacheRepository authTokensCacheRepository;

    public AuthTokens issue(AuthTokenPayload payload) {
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

    public AuthTokenPayload tokenPayload(String token) {
        return JwtUtils.tokenValue(JWT_SECRET_KEY, token, TOKEN_PAYLOAD_NAME);
    }

    public ResponseCookie accessTokenCookie(AuthTokens tokens) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, tokens.getAccessToken())
            .path("")
            .domain("ecsimsw.com")
            .sameSite("None")
            .httpOnly(true)
            .secure(true)
            .maxAge(ACCESS_TOKEN_JWT_EXPIRE_TIME)
            .build();
    }

    public ResponseCookie refreshTokenCookie(AuthTokens tokens) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, tokens.getRefreshToken())
            .path("")
            .domain("ecsimsw.com")
            .sameSite("None")
            .httpOnly(true)
            .secure(true)
            .maxAge(REFRESH_TOKEN_JWT_EXPIRE_TIME)
            .build();
    }

    public String getAccessToken(HttpServletRequest request) {
        return getToken(request, ACCESS_TOKEN_COOKIE_NAME);
    }

    public String getRefreshToken(HttpServletRequest request) {
        return getToken(request, REFRESH_TOKEN_COOKIE_NAME);
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

    private String createToken(AuthTokenPayload payload, int expiredTime) {
        return JwtUtils.createToken(JWT_SECRET_KEY, Map.of(TOKEN_PAYLOAD_NAME, payload), expiredTime);
    }
}
