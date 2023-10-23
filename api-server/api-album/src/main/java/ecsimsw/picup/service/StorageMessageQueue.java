package ecsimsw.picup.service;

import ecsimsw.picup.logging.CustomLogger;
import java.util.List;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StorageMessageQueue {

    private static final CustomLogger LOGGER = CustomLogger.init(StorageMessageQueue.class);

    private final RabbitTemplate rabbitTemplate;
    private final Queue fileDeletionQueue;

    public StorageMessageQueue(
        RabbitTemplate rabbitTemplate,
        Queue fileDeletionQueue
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.fileDeletionQueue = fileDeletionQueue;
    }

    public void requestDelete(List<String> resources) {
        rabbitTemplate.convertAndSend(fileDeletionQueue.getName(), resources);
        LOGGER.info("Queue : " + fileDeletionQueue.getName() + ", resources : " + String.join(", ", resources));
    }
}
