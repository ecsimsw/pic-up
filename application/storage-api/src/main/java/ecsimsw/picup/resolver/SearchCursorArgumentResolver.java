package ecsimsw.picup.resolver;

import ecsimsw.picup.annotation.SearchCursor;
import ecsimsw.picup.dto.PictureSearchCursor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class SearchCursorArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(SearchCursor.class);
    }

    @Override
    public PictureSearchCursor resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        var request = (HttpServletRequest) webRequest.getNativeRequest();
        var annotated = parameter.getParameterAnnotation(SearchCursor.class);
        var limit = getLimit(annotated, request);
        var createdAt = getCreatedAt(annotated, request);
        return PictureSearchCursor.from(limit, createdAt);
    }

    private int getLimit(SearchCursor annotated, HttpServletRequest request) {
        var limitParam = request.getParameter(annotated.limitHeaderName());
        if (limitParam == null) {
            return annotated.limit();
        }
        return Integer.parseInt(limitParam);
    }

    private Optional<LocalDateTime> getCreatedAt(SearchCursor annotated, HttpServletRequest request) {
        var createdAtParam = request.getParameter(annotated.createdAtHeaderName());
        if (createdAtParam == null) {
            return Optional.empty();
        }
        return Optional.of(LocalDateTime.parse(createdAtParam, DateTimeFormatter.ISO_DATE_TIME));
    }
}
