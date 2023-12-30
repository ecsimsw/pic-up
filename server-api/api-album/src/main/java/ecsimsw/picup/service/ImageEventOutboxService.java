package ecsimsw.picup.service;

import com.google.common.collect.Iterables;
import ecsimsw.picup.domain.FileDeletionEvent;
import ecsimsw.picup.domain.FileDeletionEventOutbox;
import ecsimsw.picup.domain.FileDeletionEvent_;
import ecsimsw.picup.mq.ImageFileMessageQueue;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ImageEventOutboxService {

    public final static int FILE_DELETION_SEGMENT_UNIT = 5;

    private final FileDeletionEventOutbox fileDeletionEventOutbox;
    private final ImageFileMessageQueue imageFileMessageQueue;

    public ImageEventOutboxService(
        FileDeletionEventOutbox fileDeletionEventOutbox,
        ImageFileMessageQueue imageFileMessageQueue
    ) {
        this.fileDeletionEventOutbox = fileDeletionEventOutbox;
        this.imageFileMessageQueue = imageFileMessageQueue;
    }

    @Scheduled(fixedRate = 1000)
    public void publishOut() {
        var toBeDeleted = fileDeletionEventOutbox.findAll(
            Sort.by(FileDeletionEvent_.CREATION_TIME)
        );
        for (var keySegment : Iterables.partition(toBeDeleted, FILE_DELETION_SEGMENT_UNIT)) {
            var resourceKeys = keySegment.stream()
                .map(FileDeletionEvent::getResourceKey)
                .collect(Collectors.toList());
            imageFileMessageQueue.offerDeleteAllRequest(resourceKeys);
        }
    }
}
