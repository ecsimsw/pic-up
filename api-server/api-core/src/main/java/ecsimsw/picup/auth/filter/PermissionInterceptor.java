package ecsimsw.picup.auth.filter;

import ecsimsw.picup.auth.exception.UnauthorizedException;
import ecsimsw.picup.auth.resolver.LoginUser;
import ecsimsw.picup.auth.service.AuthTokenService;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class PermissionInterceptor implements HandlerInterceptor {

    private static final List<Class<? extends Annotation>> PERMISSION_NEEDED_MARKS = List.of(LoginUser.class);
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
            if (authTokenService.hasValidAccessToken(cookies)) {
                return true;
            }
            authTokenService.reissueAuthTokens(cookies).forEach(response::addCookie);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            throw new UnauthorizedException("Unauthorized request");
        }
    }

    private boolean isLoginNeeded(HandlerMethod method) {
        return Arrays.stream(method.getMethodParameters())
            .anyMatch(this::isPermissionNeeded);
    }

    private boolean isPermissionNeeded(MethodParameter methodParameter) {
        for(Class<? extends Annotation> ano : PERMISSION_NEEDED_MARKS) {
            if(methodParameter.hasParameterAnnotation(ano)) {
                return true;
            }
        }
        return false;
    }
}
