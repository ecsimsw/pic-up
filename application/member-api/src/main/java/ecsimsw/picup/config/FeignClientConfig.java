package ecsimsw.picup.config;

import feign.Logger;
import feign.Logger.Level;
import feign.Request;
import feign.Retryer;
import java.util.concurrent.TimeUnit;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = {"ecsimsw.picup"})
public class FeignClientConfig {

    @Bean
    public Request.Options requestOptions() {
        long connectionTimeout = 3;
        long readTimeout = 3;
        return new Request.Options(connectionTimeout, TimeUnit.SECONDS, readTimeout, TimeUnit.SECONDS, false);
    }

    // IOException, Connection/Read timeout
    @Bean
    public Retryer ioExceptionRetryer() {
        return new Retryer.Default(300, TimeUnit.MICROSECONDS.toMillis(300), 3);
    }
}
