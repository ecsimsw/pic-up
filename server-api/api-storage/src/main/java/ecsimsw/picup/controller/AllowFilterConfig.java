package ecsimsw.picup.controller;

import ecsimsw.picup.auth.UnauthorizedException;
import java.util.Arrays;
import javax.servlet.Filter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AllowFilterConfig {

    private final String storageAuthKey;
    private final String storageAuthValue;

    public AllowFilterConfig(
        @Value("${storage.server.auth.key}") String storageAuthKey,
        @Value("${storage.server.auth.value}") String storageAuthValue
    ) {
        this.storageAuthKey = storageAuthKey;
        this.storageAuthValue = storageAuthValue;
    }

    @Bean
    public FilterRegistrationBean<Filter> firstFilter() {
        var registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter((request, response, chain) -> {
            var authValue = ((HttpServletRequest) request).getHeader(storageAuthKey);
            if(authValue == null || !authValue.equals(storageAuthValue)) {
                throw new UnauthorizedException("Invalid auth request");
            }
            chain.doFilter(request, response);
        });
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        registrationBean.setName("access-filter");
        return registrationBean;
    }
}
