package ecsimsw.picup.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class MessageRouteConfig {

    public static final String GLOBAL_EXCHANGE_NAME = "global.exchange";
    public static final String DEAD_LETTER_EXCHANGE_NAME = "dead.letter.exchange";

    public static final String FILE_DELETE_ALL_QUEUE_NAME = "file.deleteAll.queue";
    public static final String FILE_DELETE_ALL_QUEUE_KEY = "file.deleteAll";

    public static final String FILE_DELETE_QUEUE_NAME = "file.delete.queue";
    public static final String FILE_DELETE_QUEUE_KEY = "file.delete";

    public static final String FILE_DELETION_RECOVER_QUEUE_NAME = "file.deletion.recover.queue";
    public static final String FILE_DELETION_RECOVER_QUEUE_KEY = "file.deletion.recover";

    @Bean
    public DirectExchange globalExchange() {
        return new DirectExchange(GLOBAL_EXCHANGE_NAME);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE_NAME);
    }

    @Bean
    public Queue fileDeleteAllQueue() {
        return QueueBuilder.durable(FILE_DELETE_ALL_QUEUE_NAME)
            .deadLetterExchange(DEAD_LETTER_EXCHANGE_NAME)
            .withArguments(Map.of(
                "x-dead-letter-exchange", DEAD_LETTER_EXCHANGE_NAME,
                "x-dead-letter-routing-key", FILE_DELETION_RECOVER_QUEUE_KEY
            ))
            .build();
    }

    @Bean
    public Queue fileDeleteQueue(
    ) {
        return QueueBuilder.durable(FILE_DELETE_QUEUE_NAME)
            .deadLetterExchange(DEAD_LETTER_EXCHANGE_NAME)
            .withArguments(Map.of(
                "x-dead-letter-exchange", DEAD_LETTER_EXCHANGE_NAME,
                "x-dead-letter-routing-key", FILE_DELETION_RECOVER_QUEUE_KEY
            ))
            .build();
    }

    @Bean
    public Queue fileDeletionRecoverQueue() {
        return QueueBuilder.durable(FILE_DELETION_RECOVER_QUEUE_NAME).build();
    }

    @Bean
    public Binding fileDeleteQueueBinding(
        DirectExchange globalExchange,
        Queue fileDeleteQueue
    ) {
        return BindingBuilder
            .bind(fileDeleteQueue)
            .to(globalExchange)
            .with(FILE_DELETE_QUEUE_KEY);
    }

    @Bean
    public Binding fileDeleteAllQueueBinding(
        DirectExchange globalExchange,
        Queue fileDeleteAllQueue
    ) {
        return BindingBuilder
            .bind(fileDeleteAllQueue)
            .to(globalExchange)
            .with(FILE_DELETE_ALL_QUEUE_KEY);
    }

    @Bean
    public Binding fileDeletionRecoverQueueBinding(
        DirectExchange deadLetterExchange,
        Queue fileDeletionRecoverQueue
    ) {
        return BindingBuilder
            .bind(fileDeletionRecoverQueue)
            .to(deadLetterExchange)
            .with(FILE_DELETION_RECOVER_QUEUE_KEY);
    }
}
