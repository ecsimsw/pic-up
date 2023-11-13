package ecsimsw.picup.controller;

import ecsimsw.picup.alert.SlackMessageSender;
import ecsimsw.picup.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Controller;

import java.util.List;

import static ecsimsw.picup.mq.RabbitMQContainerFactories.FILE_DELETION_QUEUE_CF;

@Controller
public class StorageDeleteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageDeleteController.class);

    private final StorageService storageService;

    public StorageDeleteController(StorageService storageService) {
        this.storageService = storageService;
    }

    @RabbitListener(queues = "${mq.file.deletion.queue.name}", containerFactory = FILE_DELETION_QUEUE_CF)
    public void deleteAll(String resources) {
        storageService.delete(resources);
    }

    @RabbitListener(queues = "${mq.file.deletion.recover.queue.name}")
    public void deleteAllRecover(Message failedMessage) {
        final String alertMessage = "dead letter from file deletion queue \n" + "body : " + failedMessage.getPayload();
        LOGGER.error(alertMessage);
        SlackMessageSender.send(alertMessage);
    }
}
