package ecsimsw.picup.album.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(
        RestTemplateBuilder builder,
        @Value("${rt.server.connection.timeout.sec}") int connectionTimeOut,
        @Value("${rt.server.response.timeout.sec}") int responseTimeOut
    ) {
        return builder.requestFactory(() -> new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
            .setConnectTimeout(Duration.ofSeconds(connectionTimeOut))
            .setReadTimeout(Duration.ofSeconds(responseTimeOut))
            .build();
    }
}
