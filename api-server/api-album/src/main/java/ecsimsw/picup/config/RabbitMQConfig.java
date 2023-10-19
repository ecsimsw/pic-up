package ecsimsw.picup.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("password");
        return connectionFactory;
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange("hello.exchange");
    }

    @Bean
    public Queue queue1() {
        return new Queue("hello.queue1");
    }

    @Bean
    public Queue queue2() {
        return new Queue("hello.queue2");
    }

    @Bean
    public Binding binding1(DirectExchange exchange, Queue queue1) {
        return BindingBuilder
            .bind(queue1)
            .to(exchange)
            .with("hello.key");
    }

    @Bean
    public Binding binding2(DirectExchange exchange, Queue queue2) {
        return BindingBuilder
            .bind(queue2)
            .to(exchange)
            .with("hello.key");
    }
}