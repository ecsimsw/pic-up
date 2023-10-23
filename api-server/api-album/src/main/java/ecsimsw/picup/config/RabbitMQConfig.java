package ecsimsw.picup.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final int MQ_SERVER_CONNECTION_RETRY_CNT = 5;
    public static final int MQ_SERVER_CONNECTION_RETRY_DELAY_TIME_MS = 1000;

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public ConnectionFactory connectionFactory(
        @Value("${spring.rabbitmq.host}") String host,
        @Value("${spring.rabbitmq.port}") int port,
        @Value("${spring.rabbitmq.username}") String username,
        @Value("${spring.rabbitmq.password}") String password
    ) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public DirectExchange globalExchange(
        @Value("${mq.global.exchange}") String globalExchange
    ) {
        return new DirectExchange(globalExchange);
    }

    @Bean
    public Queue fileDeletionQueue(
        @Value("${mq.file.deletion.queue.name}") String queueName,
        @Value("${mq.file.deletion.queue.durable}") boolean durable,
        @Value("${mq.file.deletion.queue.exclusive}") boolean exclusive,
        @Value("${mq.file.deletion.queue.autoDelete}") boolean autoDelete
    ) {
        return new Queue(
            queueName,
            durable,
            exclusive,
            autoDelete
        );
    }

    @Bean
    public Binding fileDeletionQueueBinding(
        @Value("${mq.file.deletion.queue.key}") String fileDeletionQueueKey,
        DirectExchange globalExchange,
        Queue imageDeletionQueue
    ) {
        return BindingBuilder
            .bind(imageDeletionQueue)
            .to(globalExchange)
            .with(fileDeletionQueueKey);
    }
}
