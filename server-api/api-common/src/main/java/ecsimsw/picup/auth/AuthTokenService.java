package ecsimsw.picup.auth;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Arrays;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AuthTokenService {

    private static final Key jwtSecretKey = JwtUtils.createSecretKey("thisissecretkeythisissecretkeythisis");

    public static final String ACCESS_TOKEN_COOKIE_NAME = "PICUP_AT";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "PICUP_RT";

    public static final int REFRESH_TOKEN_JWT_EXPIRE_TIME = 2 * 60 * 60;
    public static final int ACCESS_TOKEN_JWT_EXPIRE_TIME = 30 * 60;
    private static final String TOKEN_PAYLOAD_KEY = "token";

    private final AuthTokensCacheRepository authTokensCacheRepository;

    public AuthTokens issue(AuthTokenPayload payload) {
        var tokenKey = payload.getUsername();
        if (tokenKey == null) {
            throw new IllegalArgumentException("token key must not be null");
        }
        var accessToken = createToken(payload, ACCESS_TOKEN_JWT_EXPIRE_TIME);
        var refreshToken = createToken(payload, REFRESH_TOKEN_JWT_EXPIRE_TIME);
        var authTokens = new AuthTokens(tokenKey, accessToken, refreshToken);
        authTokensCacheRepository.save(authTokens);
        return authTokens;
    }

    public void authenticate(HttpServletRequest request) {
        var cookies = request.getCookies();
        if (cookies == null) {
            throw new UnauthorizedException("Access token is not available");
        }
        var accessToken = Arrays.stream(cookies)
            .filter(cookie -> ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
           .orElseThrow(() -> new UnauthorizedException("Access token is not available"));
        if (!isValidToken(accessToken)) {
            throw new UnauthorizedException("Access token is not available");
        }
    }

    public AuthTokens reissue(HttpServletRequest request) {
        var cookies = request.getCookies();
        if (cookies == null) {
            throw new UnauthorizedException("Refresh token is not available");
        }
        var refreshToken = Arrays.stream(cookies)
            .filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElseThrow(() -> new UnauthorizedException("Refresh token is not available"));
        if (!isValidToken(refreshToken)) {
            throw new UnauthorizedException("Refresh token is not available");
        }
        var currentCachedToken = authTokensCacheRepository.findById(refreshToken)
            .orElseThrow(() -> new IllegalArgumentException("Not registered refresh token"));
        return issue(new AuthTokenPayload(currentCachedToken.getTokenKey()));
    }

    public AuthTokenPayload tokenPayload(String token) {
        return JwtUtils.tokenValue(jwtSecretKey, token, TOKEN_PAYLOAD_KEY);
    }

    private boolean isValidToken(String token) {
        try {
            JwtUtils.tokenValue(jwtSecretKey, token, TOKEN_PAYLOAD_KEY);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String createToken(AuthTokenPayload payload, int expiredTime) {
        return JwtUtils.createToken(jwtSecretKey, Map.of(TOKEN_PAYLOAD_KEY, payload), expiredTime);
    }
}
