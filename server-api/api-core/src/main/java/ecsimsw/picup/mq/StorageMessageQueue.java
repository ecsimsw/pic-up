package ecsimsw.picup.mq;

import ecsimsw.picup.mq.exception.MessageBrokerDownException;
import ecsimsw.picup.mq.message.FileDeletionRequest;
import ecsimsw.picup.storage.StorageKey;
import org.assertj.core.util.Strings;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StorageMessageQueue {

    private final RabbitTemplate rabbitTemplate;
    private final Queue fileDeleteAllQueue;
    private final Queue fileDeleteQueue;

    public StorageMessageQueue(
        RabbitTemplate rabbitTemplate,
        Queue fileDeleteAllQueue,
        Queue fileDeleteQueue
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.fileDeleteAllQueue = fileDeleteAllQueue;
        this.fileDeleteQueue = fileDeleteQueue;
    }

    @Retryable(
        label = "Retry when message server is down",
        maxAttemptsExpression = "${mq.server.connection.retry.cnt}",
        value = AmqpConnectException.class,
        backoff = @Backoff(delayExpression = "${mq.server.connection.retry.delay.time.ms}"),
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

    @Retryable(
        label = "Retry when message server is down",
        maxAttemptsExpression = "${mq.server.connection.retry.cnt}",
        value = AmqpConnectException.class,
        backoff = @Backoff(delayExpression = "${mq.server.connection.retry.delay.time.ms}"),
        recover = "recoverOfferDeleteByStorage"
    )
    public void offerDeleteByStorage(String resourceKey, StorageKey storageKey) {
        rabbitTemplate.convertAndSend(fileDeleteQueue.getName(), new FileDeletionRequest(resourceKey, storageKey));
    }

    @Recover
    public void recoverOfferDeleteByStorage(AmqpConnectException exception, String resourceKey, StorageKey storageKey) {
        var errorMessage = "Failed to connect server while deleting resources\nResource to be deleted : " + resourceKey + " in " + storageKey;
        throw new MessageBrokerDownException(errorMessage, exception);
    }
}
