package ecsimsw.picup.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = {"ecsimsw.picup"})
public class FeignClientConfig {
}
