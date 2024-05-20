package ecsimsw.picup.config;

import ecsimsw.picup.service.AuthTokenService;
import ecsimsw.picup.domain.LoginUser;
import ecsimsw.picup.annotation.TokenPayload;
import javax.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
public class AuthTokenArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthTokenService authTokenService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(TokenPayload.class);
    }

    @Override
    public LoginUser resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        var request = (HttpServletRequest) webRequest.getNativeRequest();
        var accessToken = authTokenService.getAccessToken(request);
        return authTokenService.tokenPayload(accessToken);
    }
}
