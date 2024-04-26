package ecsimsw.picup.config;

import ecsimsw.picup.logging.HttpResponseTimeAlarmFilter;
import javax.servlet.Filter;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingConfig {

    @EnabledIfEnvironmentVariable(named = "picup.log.http.response-time.alarm.enable", matches = "true")
    @Bean
    public FilterRegistrationBean<Filter> httpResponseTimeAlarmFilter(
        @Value("${picup.log.http.response-time.alarm.threshold:5}")
        double threshold
    ) {
        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new HttpResponseTimeAlarmFilter(0));
        bean.setOrder(1);
        bean.addUrlPatterns("*");
        return bean;
    }
}
