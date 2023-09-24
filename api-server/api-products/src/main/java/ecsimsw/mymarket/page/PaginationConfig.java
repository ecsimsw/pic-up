package ecsimsw.mymarket.page;

import ecsimsw.mymarket.page.resolver.AdditionalSortArgumentResolver;
import ecsimsw.mymarket.page.resolver.LimitedPaginationArgumentResolver;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PaginationConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        final var sortArgumentResolver = new AdditionalSortArgumentResolver();
        sortArgumentResolver.setSortParameter("sortBy");
        sortArgumentResolver.setPropertyDelimiter("-");

        final var paginationArgumentResolver = new LimitedPaginationArgumentResolver(sortArgumentResolver);
        paginationArgumentResolver.setOneIndexedParameters(true); // true : page start from 1
        paginationArgumentResolver.setFallbackPageable(PageRequest.of(1, 10));
        paginationArgumentResolver.setMaxPageSize(10000);
        paginationArgumentResolver.setPageParameterName("pageNumber");
        paginationArgumentResolver.setSizeParameterName("pageSize");
        argumentResolvers.add(paginationArgumentResolver);
    }
}
