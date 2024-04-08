package ecsimsw.picup.album.config;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    private static final int CONNECTION_TIMEOUT_SEC = 3;
    private static final int RESPONSE_TIMEOUT_SEC = 3;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.requestFactory(() -> new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
            .setConnectTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT_SEC))
            .setReadTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT_SEC))
            .build();
    }
}
