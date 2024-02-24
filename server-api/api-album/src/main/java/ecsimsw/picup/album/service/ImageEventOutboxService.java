package ecsimsw.picup.album.service;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ImageEventOutboxService {

    private final static int FILE_DELETION_SEGMENT_UNIT = 5;
    private final static int FILE_DELETION_RATE_SEC = 3;

    private final FileService fileService;

    @Scheduled(fixedRate = 1000 * FILE_DELETION_RATE_SEC)
    public void publishOut() {
        var toBeDeleted = fileService.findAllDeletionOutBox();
        for (var eventSegment : Iterables.partition(toBeDeleted, FILE_DELETION_SEGMENT_UNIT)) {
            fileService.publishDeletionEvents(eventSegment);
        }
    }
}
