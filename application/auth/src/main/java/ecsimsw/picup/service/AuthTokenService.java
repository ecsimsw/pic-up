package ecsimsw.picup.service;

import static ecsimsw.picup.config.AuthTokenConfig.ACCESS_TOKEN_COOKIE_NAME;
import static ecsimsw.picup.config.AuthTokenConfig.ACCESS_TOKEN_JWT_EXPIRE_TIME_SEC;
import static ecsimsw.picup.config.AuthTokenConfig.JWT_SECRET_KEY;
import static ecsimsw.picup.config.AuthTokenConfig.REFRESH_TOKEN_COOKIE_NAME;
import static ecsimsw.picup.config.AuthTokenConfig.REFRESH_TOKEN_JWT_EXPIRE_TIME_SEC;
import static ecsimsw.picup.config.AuthTokenConfig.TOKEN_PAYLOAD_NAME;

import ecsimsw.picup.config.AuthTokenConfig;
import ecsimsw.picup.domain.AuthTokens;
import ecsimsw.picup.domain.AuthTokensRepository;
import ecsimsw.picup.domain.BlockedToken;
import ecsimsw.picup.domain.BlockedTokenRepository;
import ecsimsw.picup.domain.TokenPayload;
import ecsimsw.picup.exception.UnauthorizedException;
import ecsimsw.picup.utils.JwtUtils;
import java.util.Arrays;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AuthTokenService {

    private final AuthTokensRepository authTokensRepository;
    private final BlockedTokenRepository blockedTokenRepository;

    public String createToken(TokenPayload payload, int expiredTime) {
        var payloads = Map.<String, Object>of(TOKEN_PAYLOAD_NAME, payload);
        return JwtUtils.createToken(JWT_SECRET_KEY, payloads, expiredTime);
    }

    public AuthTokens issue(TokenPayload payload) {
        var accessToken = createToken(payload, ACCESS_TOKEN_JWT_EXPIRE_TIME_SEC);
        var refreshToken = createToken(payload, REFRESH_TOKEN_JWT_EXPIRE_TIME_SEC);
        var authTokens = AuthTokens.of(payload, accessToken, refreshToken);
        authTokensRepository.save(authTokens);
        return authTokens;
    }

    public AuthTokens reissue(String refreshToken) {
        try {
            var tokenPayload = tokenPayload(refreshToken);
            authTokensRepository.findById(tokenPayload.tokenKey())
                .orElseThrow(() -> new IllegalArgumentException("Not registered refresh token"))
                .checkRefreshToken(refreshToken);
            return issue(tokenPayload);
        } catch (Exception e) {
            throw new UnauthorizedException("Refresh token is not available");
        }
    }

    public void validate(String token) {
        if(blockedTokenRepository.existsById(token)) {
            throw new UnauthorizedException("Token not usable");
        }
        tokenPayload(token);
    }

    public TokenPayload tokenPayload(String token) {
        try {
            return JwtUtils.tokenValue(JWT_SECRET_KEY, token, TOKEN_PAYLOAD_NAME);
        } catch (Exception e) {
            throw new UnauthorizedException("Token is not available");
        }
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

    public void responseAsCookies(AuthTokens authTokens, HttpServletResponse response) {
        response.addCookie(accessTokenCookie(authTokens));
        response.addCookie(refreshTokenCookie(authTokens));
    }

    public void removeTokenCookies(HttpServletResponse response) {
        var atCookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, null);
        atCookie.setMaxAge(0);
        response.addCookie(atCookie);

        var rtCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        rtCookie.setMaxAge(0);
        response.addCookie(rtCookie);
    }

    public void blockTokens(HttpServletRequest request) {
        var accessToken = getAccessToken(request);
        blockedTokenRepository.save(new BlockedToken(accessToken));

        var refreshToken = getRefreshToken(request);
        blockedTokenRepository.save(new BlockedToken(refreshToken));
    }
}
