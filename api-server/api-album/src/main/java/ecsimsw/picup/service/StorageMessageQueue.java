package ecsimsw.picup.service;

import static ecsimsw.picup.config.RabbitMQConfig.MQ_SERVER_CONNECTION_RETRY_CNT;
import static ecsimsw.picup.config.RabbitMQConfig.MQ_SERVER_CONNECTION_RETRY_DELAY_TIME_MS;

import ecsimsw.picup.exception.MessageQueueServerDownException;
import java.util.List;
import org.assertj.core.util.Strings;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class StorageMessageQueue {

    private final RabbitTemplate rabbitTemplate;
    private final Queue fileDeletionQueue;

    public StorageMessageQueue(
        RabbitTemplate rabbitTemplate,
        Queue fileDeletionQueue
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.fileDeletionQueue = fileDeletionQueue;
    }

    @Retryable(
        label = "Retry when message server is down",
        maxAttempts = MQ_SERVER_CONNECTION_RETRY_CNT,
        value = AmqpConnectException.class,
        backoff = @Backoff(delay = MQ_SERVER_CONNECTION_RETRY_DELAY_TIME_MS),
        recover = "recoverServerConnection"
    )
    public void pollDeleteRequest(List<String> resources) {
        rabbitTemplate.convertAndSend(fileDeletionQueue.getName(), resources);
    }

    @Recover
    public List<String> recoverServerConnection(Throwable exception, List<String> resources) {
        // TODO :: Manage server, resources to be deleted
        var errorMessage = "Failed to connect server while deleting resources\nResources to be deleted : " + Strings.join(resources).with(", ");
        throw new MessageQueueServerDownException(errorMessage, exception);
    }
}
