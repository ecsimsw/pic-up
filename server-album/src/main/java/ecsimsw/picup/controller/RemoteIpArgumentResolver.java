package ecsimsw.picup.controller;

import ecsimsw.picup.annotation.RemoteIp;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class RemoteIpArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RemoteIp.class);
    }

    @Override
    public String resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        var headerName = parameter.getParameterAnnotation(RemoteIp.class).headerName();
        var request = (HttpServletRequest) webRequest.getNativeRequest();
        if(request.getHeader(headerName) != null) {
            return request.getHeader(headerName);
        }
        if(request.getHeader("X-Forwarded-For") != null) {
            return request.getHeader("X-Forwarded-For");
        }
        return request.getRemoteAddr();
    }
}
