package ecsimsw.picup.mq;

import ecsimsw.picup.logging.CustomLogger;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private static final CustomLogger LOGGER = CustomLogger.init(RabbitMQConfig.class);

    @Bean
    public RabbitTemplate rabbitTemplate(
        ConnectionFactory connectionFactory
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    @Bean
    public ConnectionFactory connectionFactory(
        @Value("${spring.rabbitmq.host}") String host,
        @Value("${spring.rabbitmq.port}") int port,
        @Value("${spring.rabbitmq.username}") String username,
        @Value("${spring.rabbitmq.password}") String password
    ) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();   //  return same connection from all createConnection() calls, and ignores calls to Connection.close() and caches Channel
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.addConnectionListener(new ConnectionListener() {
            @Override
            public void onCreate(Connection connection) {
                LOGGER.info("Message queue server connection is created");
            }

            @Override
            public void onClose(Connection connection) {
                LOGGER.error("Message queue server connection is created");
            }
        });
        return connectionFactory;
    }

    @Bean
    public DirectExchange globalExchange(
        @Value("${mq.global.exchange}") String globalExchange
    ) {
        return new DirectExchange(globalExchange);
    }

    @Bean
    public DirectExchange deadLetterExchange(
        @Value("${mq.dead.letter.exchange}") String deadLetterExchange
    ) {
        return new DirectExchange(deadLetterExchange);
    }

    @Bean
    public Queue fileDeletionQueue(
        @Value("${mq.file.deletion.queue.name}") String queueName,
        @Value("${mq.dead.letter.exchange}") String deadLetterExchange,
        @Value("${mq.file.deletion.recover.queue.key}") String fileDeletionRecoverQueueKey
    ) {
        return QueueBuilder.durable(queueName)
            .deadLetterExchange(deadLetterExchange)
            .withArguments(Map.of(
                "x-dead-letter-exchange", deadLetterExchange,
                "x-dead-letter-routing-key", fileDeletionRecoverQueueKey
            ))
            .build();
    }

    @Bean
    public Queue fileDeletionRecoverQueue(
        @Value("${mq.file.deletion.recover.queue.name}") String queueName
    ) {
        return QueueBuilder.durable(queueName)
            .build();
    }

    @Bean
    public Binding fileDeletionQueueBinding(
        @Value("${mq.file.deletion.queue.key}") String fileDeletionQueueKey,
        DirectExchange globalExchange,
        Queue fileDeletionQueue
    ) {
        return BindingBuilder
            .bind(fileDeletionQueue)
            .to(globalExchange)
            .with(fileDeletionQueueKey);
    }

    @Bean
    public Binding fileDeletionRecoverQueueBinding(
        @Value("${mq.file.deletion.recover.queue.key}") String fileDeletionRecoverQueueKey,
        DirectExchange deadLetterExchange,
        Queue fileDeletionRecoverQueue
    ) {
        return BindingBuilder
            .bind(fileDeletionRecoverQueue)
            .to(deadLetterExchange)
            .with(fileDeletionRecoverQueueKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
