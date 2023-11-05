package ecsimsw.picup.auth.interceptor;

import ecsimsw.picup.auth.domain.AuthTokens;
import ecsimsw.picup.auth.exception.UnauthorizedException;
import ecsimsw.picup.auth.resolver.LoginUser;
import ecsimsw.picup.auth.service.AuthTokenService;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ecsimsw.picup.auth.service.TokenCookieUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

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
            if (!authTokenService.hasValidAccessToken(cookies)) {
                final AuthTokens reissued = authTokenService.reissue(cookies);
                TokenCookieUtils.createAuthCookies(reissued).forEach(response::addCookie);
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
