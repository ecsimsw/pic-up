package ecsimsw.picup.service;

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
        maxAttemptsExpression = "${mq.server.connection.retry.cnt}",
        value = AmqpConnectException.class,
        backoff = @Backoff(delayExpression = "${mq.server.connection.retry.delay.time.ms}"),
        recover = "recoverServerConnection"
    )
    public void pollDeleteRequest(List<String> resources) throws MessageQueueServerDownException {
        rabbitTemplate.convertAndSend(fileDeletionQueue.getName(), resources);
    }

    @Recover
    public void recoverServerConnection(Throwable exception, List<String> resources) throws MessageQueueServerDownException {
        var errorMessage = "Failed to connect server while deleting resources\nResources to be deleted : " + Strings.join(resources).with(", ");
        throw new MessageQueueServerDownException(errorMessage, exception);
    }
}
