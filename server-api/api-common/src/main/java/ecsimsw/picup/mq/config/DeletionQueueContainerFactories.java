package ecsimsw.picup.mq.config;

import java.time.Duration;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeletionQueueContainerFactories {

    public static final String FILE_DELETION_QUEUE_CF = "fileDeletionQueueContainerFactory";

    private static final int CONCURRENT_CONSUMERS = 5;
    private static final int RETRY_MAX_ATTEMPTS = 3;
    private static final int RETRY_INITIAL_INTERVAL_SEC = 1;
    private static final int RETRY_MULTIPLIER = 3;
    private static final int RETRY_MAX_INTERVAL_SEC = 10;
    private static final int PREFETCH_COUNT = 5;

    @Bean
    public RabbitListenerContainerFactory<SimpleMessageListenerContainer> fileDeletionQueueContainerFactory(
        ConnectionFactory connectionFactory,
        MessageConverter messageConverter
    ) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setMessageConverter(messageConverter);
        factory.setPrefetchCount(PREFETCH_COUNT);
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(CONCURRENT_CONSUMERS);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
            .maxAttempts(RETRY_MAX_ATTEMPTS)
            .backOffOptions(
                Duration.ofSeconds(RETRY_INITIAL_INTERVAL_SEC).toMillis(),
                RETRY_MULTIPLIER,
                Duration.ofSeconds(RETRY_MAX_INTERVAL_SEC).toMillis()
            )
            .recoverer(new RejectAndDontRequeueRecoverer())
            .build());
        return factory;
    }
}
