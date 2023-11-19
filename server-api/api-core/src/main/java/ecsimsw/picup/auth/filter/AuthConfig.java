// XXX :: This class is not in used now by changing auth from filter to interceptor
// XXX :: See "server-api/api-core/src/main/java/ecsimsw/picup/auth/interceptor/AuthInterceptor.java"
// XXX :: See "server-docs/231105 Auth Filter 를 Interceptor 로 옮기기.md"

//package ecsimsw.picup.auth.filter;
//
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//
//import javax.servlet.Filter;
//
////@Configuration
//public class AuthConfig {
//
//    private final AuthTokenFilter authTokenFilter;
//
//    public AuthConfig(AuthTokenFilter authTokenFilter) {
//        this.authTokenFilter = authTokenFilter;
//    }
//
//    @Bean
//    public FilterRegistrationBean<Filter> addAccessTokenFilter() {
//        var filterRegistrationBean = new FilterRegistrationBean<>();
//        filterRegistrationBean.setFilter(authTokenFilter);
//        filterRegistrationBean.addUrlPatterns(AuthTokenFilter.APPLY_URL_PATTERNS);
//        return filterRegistrationBean;
//    }
//}
