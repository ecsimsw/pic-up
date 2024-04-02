package ecsimsw.picup.controller;

import ecsimsw.picup.alert.SlackMessageSender;
import ecsimsw.picup.mq.message.FileDeletionRequest;
import ecsimsw.picup.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Controller;

import java.util.List;

import static ecsimsw.picup.mq.DeletionQueueContainerFactories.FILE_DELETION_QUEUE_CF;
import static ecsimsw.picup.mq.MessageRouteConfig.*;

@Controller
public class StorageDeleteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageDeleteController.class);

    private final StorageService storageService;

    public StorageDeleteController(StorageService storageService) {
        this.storageService = storageService;
    }

    @RabbitListener(queues = FILE_DELETE_ALL_QUEUE_NAME, containerFactory = FILE_DELETION_QUEUE_CF)
    public void deleteAll(List<String> resources) {
        LOGGER.info("Delete file : " + String.join("\n ", resources));
        storageService.deleteAll(resources);
    }

    @RabbitListener(queues = FILE_DELETE_QUEUE_NAME, containerFactory = FILE_DELETION_QUEUE_CF)
    public void delete(FileDeletionRequest request) {
        LOGGER.info("Delete file : " + request.getResourceKey());
        storageService.deleteByResourceKey(request.getResourceKey());
    }

    @RabbitListener(queues = FILE_DELETION_RECOVER_QUEUE_NAME)
    public void deleteAllRecover(Message failedMessage) {
        final String alertMessage = "dead letter from file deletion queue \n" + "body : " + failedMessage.getPayload();
        LOGGER.error(alertMessage);
        SlackMessageSender.send(alertMessage);
    }
}
