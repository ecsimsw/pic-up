package ecsimsw.picup.auth.service;

import ecsimsw.picup.auth.domain.AuthTokens;
import ecsimsw.picup.auth.domain.AuthTokensCacheRepository;
import ecsimsw.picup.auth.dto.AuthTokenPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import java.security.Key;
import java.util.Map;

import static ecsimsw.picup.auth.config.AuthTokenJwtConfig.*;
import static ecsimsw.picup.auth.config.AuthTokenWebConfig.*;
import static ecsimsw.picup.auth.service.TokenCookieUtils.*;

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

    public AuthTokens issueAuthTokens(Long memberId, String username) {
        authTokensCacheRepository.deleteById(username);
        final String accessToken = createAccessToken(memberId, username);
        final String refreshToken = createRefreshToken(memberId, username);
        final AuthTokens authTokens = new AuthTokens(username, accessToken, refreshToken);
        authTokensCacheRepository.save(authTokens);
        return authTokens;
    }

    public AuthTokens reissue(Cookie[] cookies) {
        final String accessToken = getTokenFromCookies(cookies, ACCESS_TOKEN_COOKIE_KEY);
        JwtUtils.requireExpired(jwtSecretKey, accessToken);
        final String refreshToken = getTokenFromCookies(cookies, REFRESH_TOKEN_COOKIE_KEY);
        JwtUtils.requireLived(jwtSecretKey, refreshToken);
        return reissue(accessToken, refreshToken);
    }

    public AuthTokens reissue(String accessToken, String refreshToken) {
        final AuthTokenPayload authTokenFromAT = JwtUtils.tokenValue(jwtSecretKey, accessToken, TOKEN_JWT_PAYLOAD_KEY, AuthTokenPayload.class, true);
        final AuthTokenPayload authTokenFromRT = JwtUtils.tokenValue(jwtSecretKey, refreshToken, TOKEN_JWT_PAYLOAD_KEY, AuthTokenPayload.class);
        authTokenFromAT.checkSameUser(authTokenFromRT);

        final String username = authTokenFromAT.getUsername();
        final AuthTokens currentAuthToken = authTokensCacheRepository.findById(username).orElseThrow(() -> new IllegalArgumentException("Not valid user"));
        currentAuthToken.checkSameWith(accessToken, refreshToken);
        return issueAuthTokens(authTokenFromAT.getId(), username);
    }

    private String createAccessToken(Long memberId, String username) {
        final Map<String, Object> payload = Map.of(TOKEN_JWT_PAYLOAD_KEY, new AuthTokenPayload(memberId, username));
        return JwtUtils.createToken(jwtSecretKey, payload, ACCESS_TOKEN_JWT_EXPIRE_TIME_SEC);
    }

    private String createRefreshToken(Long memberId, String username) {
        final Map<String, Object> payload = Map.of(TOKEN_JWT_PAYLOAD_KEY, new AuthTokenPayload(memberId, username));
        return JwtUtils.createToken(jwtSecretKey, payload, REFRESH_TOKEN_JWT_EXPIRE_TIME_SEC);
    }

    public boolean hasValidAccessToken(Cookie[] cookies) {
        try {
            final String accessToken = getTokenFromCookies(cookies, ACCESS_TOKEN_COOKIE_KEY);
            getPayloadFromToken(accessToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public AuthTokenPayload getPayloadFromToken(String accessToken) {
        return JwtUtils.tokenValue(jwtSecretKey, accessToken, TOKEN_JWT_PAYLOAD_KEY, AuthTokenPayload.class);
    }
}
