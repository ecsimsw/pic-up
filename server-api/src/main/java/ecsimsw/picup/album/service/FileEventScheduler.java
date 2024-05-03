package ecsimsw.picup.album.service;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FileEventScheduler {

    private final static int FILE_DELETION_SEGMENT_UNIT = 5;
    private final static int FILE_DELETION_SCHED_DELAY = 10_000;
    private final static int FILE_PRE_UPLOAD_SCHED_DELAY = 10_000;

    private final StorageService fileService;

    @Scheduled(fixedDelay = FILE_DELETION_SCHED_DELAY)
    public void scheduleFileDeletion() {
        var toBeDeleted = fileService.findAllDeletionEvents();
        for (var eventSegment : Iterables.partition(toBeDeleted, FILE_DELETION_SEGMENT_UNIT)) {
            fileService.deleteAll(eventSegment);
        }
    }

    @Scheduled(fixedDelay = FILE_PRE_UPLOAD_SCHED_DELAY)
    public void scheduleClearFailedToUpload() {
        fileService.deleteFailedToUploadFiles();
    }
}
