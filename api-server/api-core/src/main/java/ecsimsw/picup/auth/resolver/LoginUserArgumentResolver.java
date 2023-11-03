package ecsimsw.picup.auth.resolver;

import ecsimsw.picup.auth.dto.AuthTokenPayload;
import ecsimsw.picup.auth.exception.UnauthorizedException;
import ecsimsw.picup.auth.service.AuthTokenService;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthTokenService authTokenService;

    public LoginUserArgumentResolver(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUser.class);
    }

    @Override
    public LoginUserInfo resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        final HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        final Cookie[] cookies = request.getCookies();
        if (authTokenService.hasValidAccessToken(cookies)) {
            final AuthTokenPayload accessToken = authTokenService.authWithAccessToken(cookies);
            return LoginUserInfo.of(accessToken);
        }
        throw new UnauthorizedException("Unauthorized user request");
    }
}
