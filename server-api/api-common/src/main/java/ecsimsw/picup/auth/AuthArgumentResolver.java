package ecsimsw.picup.auth;

import static ecsimsw.picup.auth.AuthTokenService.ACCESS_TOKEN_COOKIE_NAME;

import java.util.Arrays;
import javax.servlet.http.Cookie;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

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
        var cookies = request.getCookies();
        var accessToken = Arrays.stream(cookies)
            .filter(cookie -> ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName()))
            .findFirst()
            .map(Cookie::getValue)
            .orElseThrow(() -> new UnauthorizedException("Access token is not available"));
        return authTokenService.tokenPayload(accessToken);
    }
}
