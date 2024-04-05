package ecsimsw.picup.album.service;

import com.google.common.collect.Iterables;
import ecsimsw.picup.album.utils.SchedulerLock;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class FileDeletionScheduler {

    private final static int FILE_DELETION_SEGMENT_UNIT = 5;
    private final static int FILE_DELETION_SCHED_DELAY = 3000;
    private final static int FILE_DELETION_LOCK_TIME = 30000;

    private final FileService fileService;
    private final SchedulerLock schedulerLock;

    public FileDeletionScheduler(FileService fileService, SchedulerLock schedulerLock) {
        this.fileService = fileService;
        this.schedulerLock = schedulerLock;
    }

    @Async
    public void schedulePublishOut() {
        while (true) {
            schedulerLock.afterDelay(FILE_DELETION_LOCK_TIME, FILE_DELETION_SCHED_DELAY, () -> {
                var toBeDeleted = fileService.findAllDeletionOutBox();
                for (var eventSegment : Iterables.partition(toBeDeleted, FILE_DELETION_SEGMENT_UNIT)) {
                    fileService.publishDeletionEvents(eventSegment);
                }
            });
        }
    }
}
