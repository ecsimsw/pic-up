package ecsimsw.picup.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /**
     * RabbitMQ 연결을 위한 ConnectionFactory 빈을 생성하여 반환
     *
     * @return ConnectionFactory 객체
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("password");
        return connectionFactory;
    }

    /**
     * 지정된 익스체인지 이름으로 DirectExchange 빈을 생성
     *
     * @return TopicExchange 빈 객체
     */
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange("hello.exchange");
    }


    /**
     * 지정된 큐 이름으로 Queue 빈을 생성
     *
     * @return Queue 빈 객체
     */
    @Bean
    public Queue create() {
        return new Queue("hello.queue");
    }


    /**
     * 주어진 큐와 익스체인지를 바인딩하고 라우팅 키를 사용하여 Binding 빈을 생성
     *
     * @param queue    바인딩할 Queue
     * @param exchange 바인딩할 TopicExchange
     * @return Binding 빈 객체
     */
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("hello.key");
    }

    /**
     * RabbitTemplate을 생성하여 반환
     *
     * @param connectionFactory RabbitMQ와의 연결을 위한 ConnectionFactory 객체
     * @return RabbitTemplate 객체
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    /**
     * Jackson 라이브러리를 사용하여 메시지를 JSON 형식으로 변환하는 MessageConverter 빈을 생성
     *
     * @return MessageConverter 객체
     */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}