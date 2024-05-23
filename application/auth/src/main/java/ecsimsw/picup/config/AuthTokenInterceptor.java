package ecsimsw.picup.config;

import ecsimsw.picup.service.AuthTokenService;
import ecsimsw.picup.exception.UnauthorizedException;
import ecsimsw.picup.annotation.LoginUser;
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
        try {
            if (!(handler instanceof HandlerMethod) || !isLoginNeeded((HandlerMethod) handler)) {
                return true;
            }
            if(request.getCookies() == null) {
                throw new UnauthorizedException("Login token not exists");
            }
            authTokenService.authenticate(request);
            return true;
        } catch (UnauthorizedException invalidAccessToken) {
            var reissued = authTokenService.reissue(request);

            var atCookie = authTokenService.accessTokenCookie(reissued);
            var rtCookie = authTokenService.refreshTokenCookie(reissued);
            response.addCookie(atCookie);
            response.addCookie(rtCookie);

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
