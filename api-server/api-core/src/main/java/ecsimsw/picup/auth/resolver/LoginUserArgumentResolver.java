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

import static ecsimsw.picup.auth.config.AuthTokenWebConfig.ACCESS_TOKEN_COOKIE_KEY;
import static ecsimsw.picup.auth.service.TokenCookieUtils.getTokenFromCookies;

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
        final String accessToken = getTokenFromCookies(cookies, ACCESS_TOKEN_COOKIE_KEY);
        if (authTokenService.isValidToken(accessToken)) {
            final AuthTokenPayload tokenPayload = authTokenService.getPayloadFromToken(accessToken);
            return LoginUserInfo.of(tokenPayload);
        }
        throw new UnauthorizedException("Unauthorized user request");
    }
}
