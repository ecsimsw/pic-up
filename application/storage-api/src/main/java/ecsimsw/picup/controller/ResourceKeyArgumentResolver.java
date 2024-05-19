package ecsimsw.picup.controller;

import ecsimsw.picup.domain.ResourceKey;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class ResourceKeyArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(ResourceKey.class);
    }

    @Override
    public ResourceKey resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        var parameterName = parameter.getParameterName();
        var request = (HttpServletRequest) webRequest.getNativeRequest();
        var primitiveValue = request.getParameter(parameterName);
        return new ResourceKey(primitiveValue);
    }
}
