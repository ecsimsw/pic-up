package ecsimsw.picup.auth.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.servlet.Filter;

// XXX :: LEGACY

//@Configuration
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
