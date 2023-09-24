package ecsimsw.mymarket.auth.resolver;

import ecsimsw.mymarket.auth.dto.AuthTokenPayload;
import ecsimsw.mymarket.auth.service.AuthTokenService;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

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
        final Cookie[] cookies = ((HttpServletRequest) webRequest.getNativeRequest()).getCookies();
        final AuthTokenPayload accessToken = authTokenService.authWithAccessToken(cookies);
        return LoginUserInfo.of(accessToken);
    }
}
