package ecsimsw.picup.integration;

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import redis.embedded.RedisServer;

@Slf4j
@TestConfiguration
public class RedisConfig {

    private final RedisServer redisServer;
    public final int port;

    public RedisConfig(@Value("${spring.redis.port}") int port) throws IOException {
        this.port = port;
        this.redisServer = new RedisServer(port);
    }

    @PostConstruct
    public void postConstruct() throws IOException {
        if (!redisServer.isActive()) {
            log.info("==== Embedded redis start ====");
            redisServer.start();
        }
    }

    @PreDestroy
    public void preDestroy() throws IOException {
        redisServer.stop();
    }

    @Primary
    @Bean
    public LettuceConnectionFactory getLettuceConnectionFactory() {
        return new LettuceConnectionFactory("localhost", port);
    }
}
