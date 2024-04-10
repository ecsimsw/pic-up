package ecsimsw.picup.mq;

import org.assertj.core.util.Strings;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageFileMessageQueue {

    private final RabbitTemplate rabbitTemplate;
    private final Queue fileDeleteAllQueue;

    public ImageFileMessageQueue(
        RabbitTemplate rabbitTemplate,
        Queue fileDeleteAllQueue
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.fileDeleteAllQueue = fileDeleteAllQueue;
    }

    @Retryable(
        label = "Retry when message server is down",
        maxAttempts = RabbitMQConfig.CONNECTION_RETRY_COUNT,
        value = AmqpConnectException.class,
        recover = "recoverOfferDeleteAllRequest"
    )
    public void offerDeleteAllRequest(List<String> resources) {
        rabbitTemplate.convertAndSend(fileDeleteAllQueue.getName(), resources);
    }

    @Recover
    public void recoverOfferDeleteAllRequest(AmqpConnectException exception, List<String> resources) {
        var errorMessage = "Failed to connect server while deleting resources\nResources to be deleted : " + Strings.join(resources).with(", ");
        throw new MessageBrokerDownException(errorMessage, exception);
    }
}
