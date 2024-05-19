package ecsimsw.picup.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

import static ecsimsw.picup.config.AuthConfig.*;

@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

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

            var atCookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, reissue.getAccessToken());
            atCookie.setHttpOnly(true);
            atCookie.setMaxAge(ACCESS_TOKEN_JWT_EXPIRE_TIME);

            var rtCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, reissue.getAccessToken());
            rtCookie.setHttpOnly(true);
            rtCookie.setMaxAge(REFRESH_TOKEN_JWT_EXPIRE_TIME);

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
