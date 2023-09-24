package ecsimsw.mymarket.auth;

import ecsimsw.mymarket.auth.filter.AuthTokenFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class AuthConfig {

    private final AuthTokenFilter authTokenFilter;

    public AuthConfig(AuthTokenFilter authTokenFilter) {
        this.authTokenFilter = authTokenFilter;
    }

    @Bean
    public FilterRegistrationBean<Filter> addAccessTokenFilter() {
        var filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(authTokenFilter);
        filterRegistrationBean.addUrlPatterns(AuthTokenFilter.APPLY_URL_PATTERNS);
        return filterRegistrationBean;
    }
}
