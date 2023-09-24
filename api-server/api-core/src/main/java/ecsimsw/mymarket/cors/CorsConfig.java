package ecsimsw.mymarket.cors;

import ecsimsw.mymarket.cors.filter.CorsFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.servlet.Filter;

@Configuration
public class CorsConfig {

    private final CorsFilter corsFilter;

    public CorsConfig(CorsFilter corsFilter) {
        this.corsFilter = corsFilter;
    }

    @Bean
    public FilterRegistrationBean<Filter> addCorsFilter() {
        var filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(corsFilter);
        filterRegistrationBean.addUrlPatterns(CorsFilter.APPLY_URL_PATTERNS);
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }
}
