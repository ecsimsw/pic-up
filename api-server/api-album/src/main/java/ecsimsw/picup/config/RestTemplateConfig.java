package ecsimsw.picup.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    public static final int SERVER_CONNECTION_RETRY_CNT = 5;
    public static final int SERVER_CONNECTION_RETRY_DELAY_TIME_MS = 5000;

    public static final int SERVER_CONNECTION_TIMEOUT_SEC = 5;
    public static final int SERVER_READ_TIMEOUT_SEC = 5;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.requestFactory(() -> new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
            .setConnectTimeout(Duration.ofSeconds(SERVER_CONNECTION_TIMEOUT_SEC))
            .setReadTimeout(Duration.ofSeconds(SERVER_READ_TIMEOUT_SEC))
            .build();
    }
}
