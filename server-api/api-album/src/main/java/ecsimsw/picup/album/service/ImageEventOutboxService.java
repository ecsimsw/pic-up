package ecsimsw.picup.album.service;

import com.google.common.collect.Iterables;
import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.domain.FileDeletionEventOutbox;
import ecsimsw.picup.album.domain.FileDeletionEvent_;
import ecsimsw.picup.alert.SlackMessageSender;
import ecsimsw.picup.mq.ImageFileMessageQueue;
import ecsimsw.picup.mq.exception.MessageBrokerDownException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ImageEventOutboxService {

    private final Logger LOGGER = LoggerFactory.getLogger(ImageEventOutboxService.class);
    private final static int FILE_DELETION_SEGMENT_UNIT = 5;

    private final FileDeletionEventOutbox fileDeletionEventOutbox;
    private final ImageFileMessageQueue imageFileMessageQueue;

    @Scheduled(fixedRate = 1000)
    public void publishOut() {
        var toBeDeleted = fileDeletionEventOutbox.findAll(
            Sort.by(FileDeletionEvent_.CREATION_TIME)
        );
        for (var eventSegment : Iterables.partition(toBeDeleted, FILE_DELETION_SEGMENT_UNIT)) {
            publishDeletionEvents(eventSegment);
        }
    }

    @Transactional
    public void publishDeletionEvents(List<FileDeletionEvent> events) {
        try {
            var resourceKeys = events.stream()
                .map(FileDeletionEvent::getResourceKey)
                .collect(Collectors.toList());
            imageFileMessageQueue.offerDeleteAllRequest(resourceKeys);
            fileDeletionEventOutbox.deleteAll(events);
            LOGGER.info("publish deletion event : " + String.join(", ", resourceKeys));
        } catch (MessageBrokerDownException e) {
            var alertMessage = "[MESSAGE_BROKER_CONNECT_] : " + e.getMessage();
            LOGGER.error(alertMessage + "\n" + e.getCause());
            SlackMessageSender.send(alertMessage);
        }
    }
}
