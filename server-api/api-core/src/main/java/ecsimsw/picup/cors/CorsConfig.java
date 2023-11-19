// XXX :: This class is not in used now by changing auth from filter to interceptor
// XXX :: See "server-api/api-core/src/main/java/ecsimsw/picup/auth/interceptor/AuthInterceptor.java"
// XXX :: See "server-docs/231105 Auth Filter 를 Interceptor 로 옮기기.md"

//package ecsimsw.picup.cors;
//
//import ecsimsw.picup.cors.filter.CorsFilter;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.Ordered;
//
//import javax.servlet.Filter;
//
//@Configuration
//public class CorsConfig {
//
//    private final CorsFilter corsFilter;
//
//    public CorsConfig(CorsFilter corsFilter) {
//        this.corsFilter = corsFilter;
//    }
//
//    @Bean
//    public FilterRegistrationBean<Filter> addCorsFilter() {
//        var filterRegistrationBean = new FilterRegistrationBean<>();
//        filterRegistrationBean.setFilter(corsFilter);
//        filterRegistrationBean.addUrlPatterns(CorsFilter.APPLY_URL_PATTERNS);
//        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
//        return filterRegistrationBean;
//    }
//}
