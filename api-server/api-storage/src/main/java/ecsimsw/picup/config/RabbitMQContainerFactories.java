package ecsimsw.picup.config;

import java.time.Duration;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
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

    /*
        initial-interval : 최초 재시도 간격 시간
        max-interval : 재시도 간격 시간
        max-attempts : 재시도 최대 횟수
        multiplier : 재시도 간격 시간을 배수로 점진적으로 높인다.
     */

    @Bean
    SimpleRabbitListenerContainerFactory listenerContainer(
        ConnectionFactory connectionFactory
    ) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
            .maxAttempts(3)
            .backOffOptions(Duration.ofSeconds(3L).toMillis(), 2, Duration.ofSeconds(10L).toMillis())
            .recoverer(new RejectAndDontRequeueRecoverer())
            .build());
        return factory;
    }

    @Bean
    SimpleRabbitListenerContainerFactory deadListenerContainer(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory container = new SimpleRabbitListenerContainerFactory();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}
