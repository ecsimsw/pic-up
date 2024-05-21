package ecsimsw.picup.config;

import ecsimsw.picup.service.AuthTokenService;
import ecsimsw.picup.exception.UnauthorizedException;
import ecsimsw.picup.annotation.TokenPayload;
import java.util.Arrays;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
public class AuthTokenInterceptor implements HandlerInterceptor {

    private final AuthTokenService authTokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            if (!(handler instanceof HandlerMethod) || !isLoginNeeded((HandlerMethod) handler)) {
                return true;
            }
            authTokenService.authenticate(request);
            return true;
        } catch (UnauthorizedException invalidAccessToken) {
            var reissue = authTokenService.reissue(request);

            var atCookie = new Cookie(AuthTokenConfig.ACCESS_TOKEN_COOKIE_NAME, reissue.getAccessToken());
            atCookie.setHttpOnly(true);
            atCookie.setMaxAge(AuthTokenConfig.ACCESS_TOKEN_JWT_EXPIRE_TIME);

            var rtCookie = new Cookie(AuthTokenConfig.REFRESH_TOKEN_COOKIE_NAME, reissue.getAccessToken());
            rtCookie.setHttpOnly(true);
            rtCookie.setMaxAge(AuthTokenConfig.REFRESH_TOKEN_JWT_EXPIRE_TIME);

            response.setHeader("Location", request.getRequestURI());
            response.setStatus(HttpStatus.PERMANENT_REDIRECT.value());
            return false;
        }
    }

    private boolean isLoginNeeded(HandlerMethod method) {
        return Arrays.stream(method.getMethodParameters())
            .anyMatch(methodParameter -> methodParameter.hasParameterAnnotation(TokenPayload.class));
    }
}
