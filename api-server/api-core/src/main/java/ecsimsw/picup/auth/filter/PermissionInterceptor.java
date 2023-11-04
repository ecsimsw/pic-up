package ecsimsw.picup.auth.filter;

import ecsimsw.picup.auth.exception.UnauthorizedException;
import ecsimsw.picup.auth.resolver.LoginUser;
import ecsimsw.picup.auth.service.AuthTokenService;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class PermissionInterceptor implements HandlerInterceptor {

    private final AuthTokenService authTokenService;

    public PermissionInterceptor(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!isLoginNeeded((HandlerMethod) handler)) {
            return true;
        }
        try {
            var cookies = request.getCookies();
            if (!authTokenService.hasValidAccessToken(cookies)) {
                authTokenService.reissueAuthTokens(cookies).forEach(response::addCookie);
            }
            return true;
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
}
