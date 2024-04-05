package ecsimsw.picup.auth;

import java.util.Arrays;
import javax.servlet.http.Cookie;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

import static ecsimsw.picup.auth.AuthConfig.ACCESS_TOKEN_COOKIE_NAME;

@Component
public class AuthArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthTokenService authTokenService;

    public AuthArgumentResolver(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(TokenPayload.class);
    }

    @Override
    public AuthTokenPayload resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        var request = (HttpServletRequest) webRequest.getNativeRequest();
        var accessToken = authTokenService.getAccessToken(request);
        return authTokenService.tokenPayload(accessToken);
    }
}
