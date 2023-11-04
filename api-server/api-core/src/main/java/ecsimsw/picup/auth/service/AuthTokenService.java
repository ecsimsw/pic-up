package ecsimsw.picup.auth.service;

import ecsimsw.picup.auth.domain.AuthTokens;
import ecsimsw.picup.auth.domain.AuthTokensCacheRepository;
import ecsimsw.picup.auth.dto.AuthTokenPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import java.security.Key;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static ecsimsw.picup.auth.config.AuthTokenJwtConfig.*;
import static ecsimsw.picup.auth.config.AuthTokenWebConfig.*;

@Service
public class AuthTokenService {

    private final AuthTokensCacheRepository authTokensCacheRepository;

    private final Key jwtSecretKey;

    public AuthTokenService(
        @Value("${token.secret}") String jwtSecretKey,
        AuthTokensCacheRepository authTokensCacheRepository
    ) {
        this.jwtSecretKey = JwtUtils.createSecretKey(jwtSecretKey);
        this.authTokensCacheRepository = authTokensCacheRepository;
    }

    public List<Cookie> issueAuthTokens(Long memberId, String username) {
        final String accessToken = createAccessToken(memberId, username);
        final String refreshToken = createRefreshToken(memberId, username);
        final AuthTokens authTokens = new AuthTokens(username, accessToken, refreshToken);
        authTokensCacheRepository.deleteById(authTokens.getUsername());
        authTokensCacheRepository.save(authTokens);
        final Cookie accessTokenCookie = createAccessTokenCookie(authTokens.getAccessToken());
        final Cookie refreshTokenCookie = createRefreshTokenCookie(authTokens.getRefreshToken());
        return List.of(accessTokenCookie, refreshTokenCookie);
    }

    public List<Cookie> reissueAuthTokens(Cookie[] cookies) {
        final String accessToken = getTokenFromCookies(cookies, ACCESS_TOKEN_COOKIE_KEY);
        JwtUtils.requireExpired(jwtSecretKey, accessToken);
        final String refreshToken = getTokenFromCookies(cookies, REFRESH_TOKEN_COOKIE_KEY);
        JwtUtils.requireLived(jwtSecretKey, refreshToken);

        final AuthTokenPayload authTokenFromAT = JwtUtils.tokenValue(jwtSecretKey, accessToken, TOKEN_JWT_PAYLOAD_KEY, AuthTokenPayload.class, true);
        final AuthTokenPayload authTokenFromRT = JwtUtils.tokenValue(jwtSecretKey, refreshToken, TOKEN_JWT_PAYLOAD_KEY, AuthTokenPayload.class);
        authTokenFromAT.checkSameUser(authTokenFromRT);

        final Long memberId = authTokenFromAT.getId();
        final String username = authTokenFromAT.getUsername();
        final AuthTokens currentAuthToken = authTokensCacheRepository.findById(username)
            .orElseThrow(() -> new IllegalArgumentException("Not valid user"));
        currentAuthToken.checkSameWith(accessToken, refreshToken);
        return issueAuthTokens(memberId, username);
    }

    private String createAccessToken(Long memberId, String username) {
        final Map<String, Object> payload = Map.of(TOKEN_JWT_PAYLOAD_KEY, new AuthTokenPayload(memberId, username));
        return JwtUtils.createToken(jwtSecretKey, payload, ACCESS_TOKEN_JWT_EXPIRE_TIME);
    }

    private String createRefreshToken(Long memberId, String username) {
        final Map<String, Object> payload = Map.of(TOKEN_JWT_PAYLOAD_KEY, new AuthTokenPayload(memberId, username));
        return JwtUtils.createToken(jwtSecretKey, payload, REFRESH_TOKEN_JWT_EXPIRE_TIME);
    }

    public boolean hasValidAccessToken(Cookie[] cookies) {
        try {
            authWithAccessToken(cookies);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public AuthTokenPayload authWithAccessToken(Cookie[] cookies) {
        final String accessToken = getTokenFromCookies(cookies, ACCESS_TOKEN_COOKIE_KEY);
        return JwtUtils.tokenValue(jwtSecretKey, accessToken, TOKEN_JWT_PAYLOAD_KEY, AuthTokenPayload.class);
    }

    private String getTokenFromCookies(Cookie[] cookies, String tokenCookieKey) {
        if (cookies == null) {
            throw new IllegalArgumentException("Not authorized - No cookie");
        }
        return Arrays.stream(cookies)
            .filter(cookie -> tokenCookieKey.equals(cookie.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Not authorized"))
            .getValue();
    }

    public Cookie createAccessTokenCookie(String accessToken) {
        final Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_KEY, accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(ACCESS_TOKEN_COOKIE_TTL);
        cookie.setSecure(false);
        return cookie;
    }

    public Cookie createRefreshTokenCookie(String refreshToken) {
        final Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_KEY, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(REFRESH_TOKEN_COOKIE_TTL);
        cookie.setSecure(false);
        return cookie;
    }
}
