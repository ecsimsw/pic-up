package ecsimsw.picup.config;

import java.time.Duration;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQContainerFactories {

    public static final String FILE_DELETION_QUEUE_CF = "fileDeletionQueueContainerFactory";

    @Bean
    public RabbitListenerContainerFactory<SimpleMessageListenerContainer> fileDeletionQueueContainerFactory(
        ConnectionFactory connectionFactory,
        @Value("${mq.file.deletion.queue.prefetch:20}") int prefetch
    ) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setPrefetchCount(prefetch);
        factory.setConnectionFactory(connectionFactory);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
            .maxAttempts(3)
            .backOffOptions(Duration.ofSeconds(1L).toMillis(), 3, Duration.ofSeconds(10L).toMillis())
            .recoverer(new RejectAndDontRequeueRecoverer())
            .build());
        return factory;
    }
}
