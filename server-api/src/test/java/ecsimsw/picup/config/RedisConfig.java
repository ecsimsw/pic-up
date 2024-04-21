package ecsimsw.picup.config;

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import redis.embedded.RedisServer;

@TestConfiguration
public class RedisConfig {

    private final RedisServer redisServer;
    public final int port;

    public RedisConfig(
        @Value("${spring.redis.port}") int port
    ) throws IOException {
        this.port = port;
        this.redisServer = new RedisServer(port);
    }

    @PostConstruct
    public void postConstruct() throws IOException {
        if (!redisServer.isActive()) {
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
