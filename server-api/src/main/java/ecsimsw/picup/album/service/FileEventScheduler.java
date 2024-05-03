package ecsimsw.picup.album.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FileEventScheduler {

    private final static int FILE_DELETION_SCHED_DELAY = 10_000;

    private final StorageService fileService;

    @Scheduled(fixedDelay = FILE_DELETION_SCHED_DELAY)
    public void scheduleDelete() {
        fileService.deleteAllDummies();
    }
}
