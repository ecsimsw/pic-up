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

@Service
public class AuthTokenService {

    private final Key key;
    private final int accessTokenJwtExpireTime;
    private final String accessTokenPayloadKey;
    private final int refreshTokenJwtExpireTime;
    private final String refreshTokenPayloadKey;
    private final String accessTokenCookieKey;
    private final int accessTokenCookieTTL;
    private final String refreshTokenCookieKey;
    private final int refreshTokenCookieTTL;
    private final AuthTokensCacheRepository authTokensCacheRepository;

    public AuthTokenService(
        @Value("${token.secret}") String key,
        @Value("${access.token.jwt.expireTime}") int accessTokenJwtExpireTime,
        @Value("${access.token.jwt.payload.key}") String accessTokenPayloadKey,
        @Value("${refresh.token.jwt.expireTime}") int refreshTokenJwtExpireTime,
        @Value("${refresh.token.jwt.payload.key}") String refreshTokenPayloadKey,
        @Value("${access.token.cookie.key}") String accessTokenCookieKey,
        @Value("${access.token.cookie.TTL}") int accessTokenCookieTTL,
        @Value("${refresh.token.cookie.key}") String refreshTokenCookieKey,
        @Value("${refresh.token.cookie.TTL}") int refreshTokenCookieTTL,
        AuthTokensCacheRepository authTokensCacheRepository
    ) {
        this.key = JwtUtils.createSecretKey(key);
        this.accessTokenJwtExpireTime = accessTokenJwtExpireTime;
        this.accessTokenPayloadKey = accessTokenPayloadKey;
        this.refreshTokenJwtExpireTime = refreshTokenJwtExpireTime;
        this.refreshTokenPayloadKey = refreshTokenPayloadKey;
        this.accessTokenCookieKey = accessTokenCookieKey;
        this.accessTokenCookieTTL = accessTokenCookieTTL;
        this.refreshTokenCookieKey = refreshTokenCookieKey;
        this.refreshTokenCookieTTL = refreshTokenCookieTTL;
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
        final String accessToken = getTokenFromCookies(cookies, accessTokenCookieKey);
        JwtUtils.requireExpired(key, accessToken);
        final String refreshToken = getTokenFromCookies(cookies, refreshTokenCookieKey);
        JwtUtils.requireLived(key, refreshToken);

        final AuthTokenPayload authTokenFromAT = JwtUtils.tokenValue(key, accessToken, accessTokenPayloadKey, AuthTokenPayload.class, true);
        final AuthTokenPayload authTokenFromRT = JwtUtils.tokenValue(key, refreshToken, refreshTokenPayloadKey, AuthTokenPayload.class);
        authTokenFromAT.checkSameUser(authTokenFromRT);

        final Long memberId = authTokenFromAT.getId();
        final String username = authTokenFromAT.getUsername();
        final AuthTokens currentAuthToken = authTokensCacheRepository.findById(username)
            .orElseThrow(() -> new IllegalArgumentException("Not valid user"));
        currentAuthToken.checkSameWith(accessToken, refreshToken);
        return issueAuthTokens(memberId, username);
    }

    private String createAccessToken(Long memberId, String username) {
        final Map<String, Object> payload = Map.of(accessTokenPayloadKey, new AuthTokenPayload(memberId, username));
        return JwtUtils.createToken(key, payload, accessTokenJwtExpireTime);
    }

    private String createRefreshToken(Long memberId, String username) {
        final Map<String, Object> payload = Map.of(refreshTokenPayloadKey, new AuthTokenPayload(memberId, username));
        return JwtUtils.createToken(key, payload, refreshTokenJwtExpireTime);
    }

    public boolean hasValidAccessToken(Cookie[] cookies) {
        try {
            authWithAccessToken(cookies);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public AuthTokenPayload authWithAccessToken(Cookie[] cookies) {
        final String accessToken = getTokenFromCookies(cookies, accessTokenCookieKey);
        return JwtUtils.tokenValue(key, accessToken, accessTokenPayloadKey, AuthTokenPayload.class);
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
        final Cookie cookie = new Cookie(accessTokenCookieKey, accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(accessTokenCookieTTL);
        cookie.setSecure(false);
        return cookie;
    }

    public Cookie createRefreshTokenCookie(String refreshToken) {
        final Cookie cookie = new Cookie(refreshTokenCookieKey, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(refreshTokenCookieTTL);
        cookie.setSecure(false);
        return cookie;
    }
}
