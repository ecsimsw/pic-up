package ecsimsw.picup.auth.interceptor;

import ecsimsw.picup.auth.exception.TokenException;
import ecsimsw.picup.auth.exception.UnauthorizedException;
import ecsimsw.picup.auth.resolver.LoginUser;
import ecsimsw.picup.auth.service.AuthTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

import static ecsimsw.picup.auth.config.AuthTokenWebConfig.ACCESS_TOKEN_COOKIE_KEY;
import static ecsimsw.picup.auth.config.AuthTokenWebConfig.REFRESH_TOKEN_COOKIE_KEY;
import static ecsimsw.picup.auth.service.TokenCookieUtils.createAuthCookies;
import static ecsimsw.picup.auth.service.TokenCookieUtils.getTokenFromCookies;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthInterceptor.class);

    private final AuthTokenService authTokenService;

    public AuthInterceptor(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod) || !isLoginNeeded((HandlerMethod) handler)) {
            return true;
        }
        try {
            var cookies = request.getCookies();
            var accessToken = accessToken(cookies);
            if (authTokenService.isValidToken(accessToken)) {
                return true;
            }
            var refreshToken = refreshToken(cookies);
            if (authTokenService.isValidToken(refreshToken)) {
                var reissued = authTokenService.validateAndReissue(accessToken, refreshToken);
                LOGGER.info("Refresh token reissued");
                var newAuthCookies = createAuthCookies(reissued);
                newAuthCookies.forEach(response::addCookie);
                response.setHeader("Location", request.getRequestURI());
                response.setStatus(HttpStatus.PERMANENT_REDIRECT.value());
                return false;
            }
            throw new TokenException("Invalid token");
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            throw new UnauthorizedException("Unauthorized request");
        }
    }

    private boolean isLoginNeeded(HandlerMethod method) {
        return Arrays.stream(method.getMethodParameters())
            .anyMatch(methodParameter -> methodParameter.hasParameterAnnotation(LoginUser.class)
        );
    }

    private String accessToken(Cookie[] cookies) {
        return getTokenFromCookies(cookies, ACCESS_TOKEN_COOKIE_KEY);
    }

    private String refreshToken(Cookie[] cookies) {
        return getTokenFromCookies(cookies, REFRESH_TOKEN_COOKIE_KEY);
    }
}
