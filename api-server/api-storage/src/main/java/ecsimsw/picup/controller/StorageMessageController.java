package ecsimsw.picup.controller;

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

    @RabbitListener(queues = "${mq.file.deletion.queue.name}", containerFactory = "fileDeletionQueueCF")
    public void deleteAll(List<String> resources) {
        LOGGER.info("poll to be deleted resources : " + String.join(", ", resources));
        final List<String> deleted = storageService.deleteAll(resources);
        LOGGER.info("actual deleted resources : " + String.join(", ", deleted));
    }
}
