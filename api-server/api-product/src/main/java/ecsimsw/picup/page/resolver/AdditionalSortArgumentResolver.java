package ecsimsw.picup.page.resolver;

import ecsimsw.picup.page.annotation.AdditionalSortParameter;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public class AdditionalSortArgumentResolver extends SortHandlerMethodArgumentResolver {

    @Override
    public Sort resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        final var resolvedSort = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        if(parameter.hasMethodAnnotation(AdditionalSortParameter.class)) {
            final var property = parameter.getMethodAnnotation(AdditionalSortParameter.class).property();
            final var direction = parameter.getMethodAnnotation(AdditionalSortParameter.class).direction();
            return resolvedSort.and(Sort.by(new Order(direction, property)));
        }
        return resolvedSort;
    }
}
