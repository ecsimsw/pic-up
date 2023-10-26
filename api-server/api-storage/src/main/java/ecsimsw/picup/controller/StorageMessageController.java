package ecsimsw.picup.controller;

import static ecsimsw.picup.config.RabbitMQContainerFactories.FILE_DELETION_QUEUE_CF;

import ecsimsw.picup.logging.CustomLogger;
import ecsimsw.picup.service.StorageService;
import java.util.List;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Controller;

@Controller
public class StorageMessageController {

    private static final CustomLogger LOGGER = CustomLogger.init(StorageMessageController.class);

    private final StorageService storageService;

    public StorageMessageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @RabbitListener(queues = "${mq.file.deletion.queue.name}", containerFactory = FILE_DELETION_QUEUE_CF)
    public void deleteAll(List<String> resources) {
        storageService.deleteAll(resources);
    }

    @RabbitListener(queues = "${mq.file.deletion.recover.queue.name}", containerFactory = FILE_DELETION_QUEUE_CF)
    public void deleteAllRecover(List<String> resources) {
        LOGGER.error("dead letter from file deletion queue : " + String.join(", ", resources));
    }
}
