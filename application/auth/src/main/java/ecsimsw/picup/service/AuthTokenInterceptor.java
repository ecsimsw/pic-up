package ecsimsw.picup.service;

import ecsimsw.picup.annotation.LoginUser;
import ecsimsw.picup.exception.UnauthorizedException;
import java.util.Arrays;
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
        if (!(handler instanceof HandlerMethod) || !isLoginNeeded((HandlerMethod) handler)) {
            return true;
        }
        try {
            var accessToken = authTokenService.getAccessToken(request);
            authTokenService.validate(accessToken);
            return true;
        } catch (UnauthorizedException invalidAccessToken) {
            var refreshToken = authTokenService.getRefreshToken(request);
            authTokenService.validate(refreshToken);

            var reissued = authTokenService.reissue(refreshToken);
            authTokenService.responseAsCookies(reissued, response);

            response.setHeader("Location", request.getRequestURI());
            response.setStatus(HttpStatus.PERMANENT_REDIRECT.value());
            return false;
        }
    }

    private boolean isLoginNeeded(HandlerMethod method) {
        return Arrays.stream(method.getMethodParameters())
            .anyMatch(methodParameter -> methodParameter.hasParameterAnnotation(LoginUser.class));
    }
}
