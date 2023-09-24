package ecsimsw.mymarket.page.resolver;

import ecsimsw.mymarket.page.annotation.LimitedSizePagination;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public class LimitedPaginationArgumentResolver extends PageableHandlerMethodArgumentResolver {

    public LimitedPaginationArgumentResolver(SortHandlerMethodArgumentResolver sortResolver) {
        super(sortResolver);
    }

    @Override
    public Pageable resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        final var requestPageable = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        if (parameter.hasMethodAnnotation(LimitedSizePagination.class)) {
            final var maxSize = parameter.getMethodAnnotation(LimitedSizePagination.class).maxSize();
            if (requestPageable.getPageSize() > maxSize) {
                throw new IllegalArgumentException("page size can't be bigger than " + maxSize);
            }
        }
        return requestPageable;
    }
}
