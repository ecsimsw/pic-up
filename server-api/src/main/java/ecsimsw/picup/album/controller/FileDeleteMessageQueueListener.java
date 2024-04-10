package ecsimsw.picup.album.controller;

import ecsimsw.picup.album.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Controller;

import java.util.List;

import static ecsimsw.picup.mq.DeletionQueueContainerFactories.FILE_DELETION_QUEUE_CF;
import static ecsimsw.picup.mq.MessageRouteConfig.FILE_DELETE_ALL_QUEUE_NAME;
import static ecsimsw.picup.mq.MessageRouteConfig.FILE_DELETION_RECOVER_QUEUE_NAME;

@Controller
public class FileDeleteMessageQueueListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDeleteMessageQueueListener.class);

    private final FileStorageService storageService;

    public FileDeleteMessageQueueListener(FileStorageService storageService) {
        this.storageService = storageService;
    }

    @RabbitListener(queues = FILE_DELETE_ALL_QUEUE_NAME, containerFactory = FILE_DELETION_QUEUE_CF)
    public void deleteAll(List<String> resources) {
        LOGGER.info("Delete file : " + String.join("\n ", resources));
        resources.forEach(storageService::delete);
    }

    @RabbitListener(queues = FILE_DELETION_RECOVER_QUEUE_NAME)
    public void deleteAllRecover(Message failedMessage) {
        var alertMessage = "dead letter from file deletion queue \n" + "body : " + failedMessage.getPayload();
        LOGGER.error(alertMessage);
    }
}
