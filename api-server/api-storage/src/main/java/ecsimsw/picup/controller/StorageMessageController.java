package ecsimsw.picup.controller;

import static ecsimsw.picup.config.RabbitMQContainerFactories.FILE_DELETION_QUEUE_CF;

import ecsimsw.picup.logging.CustomLogger;
import ecsimsw.picup.service.StorageServiceBackUp;

import java.util.List;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Controller;

@Controller
public class StorageMessageController {

    private static final CustomLogger LOGGER = CustomLogger.init(StorageMessageController.class);

    private final StorageServiceBackUp storageServiceBackUp;

    public StorageMessageController(StorageServiceBackUp storageServiceBackUp) {
        this.storageServiceBackUp = storageServiceBackUp;
    }

    @RabbitListener(queues = "${mq.file.deletion.queue.name}", containerFactory = FILE_DELETION_QUEUE_CF)
    public void deleteAll(List<String> resources) {
        storageServiceBackUp.deleteAll(resources);
    }

    @RabbitListener(queues = "${mq.file.deletion.recover.queue.name}")
    public void deleteAllRecover(Message failedMessage) {
        LOGGER.error("dead letter from file deletion queue \n" +
            "body : " + failedMessage.getPayload());
    }
}
