package ecsimsw.picup.album.service;

import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ImageEventOutboxService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ImageEventOutboxService.class);

    private final static int FILE_DELETION_SEGMENT_UNIT = 5;
    private final static int FILE_DELETION_SCHED_DELAY = 3000;
    private final static int FILE_DELETION_LOCK_TIME = 30000;

    private final FileService fileService;
    private final SchedulerLock schedulerLock;

    public ImageEventOutboxService(FileService fileService, SchedulerLock schedulerLock) {
        this.fileService = fileService;
        this.schedulerLock = schedulerLock;
    }

    @Async
    public void schedulePublishOut() {
        while (true) {
            schedulerLock.afterDelay(FILE_DELETION_LOCK_TIME, FILE_DELETION_SCHED_DELAY, () -> {
                LOGGER.info("outbox scheduled");
                var toBeDeleted = fileService.findAllDeletionOutBox();
                for (var eventSegment : Iterables.partition(toBeDeleted, FILE_DELETION_SEGMENT_UNIT)) {
                    fileService.publishDeletionEvents(eventSegment);
                }
            });
        }
    }
}
