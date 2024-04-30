package ecsimsw.picup.config.dev;

import ecsimsw.picup.album.controller.RemoteIpArgumentResolver;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

@Primary
@Profile("dev")
@Component
public class MockRemoteIpArgumentResolver extends RemoteIpArgumentResolver {

    @Override
    public String resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return "127.0.0.1";
    }
}

