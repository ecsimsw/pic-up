package ecsimsw.picup.application;

import ecsimsw.picup.domain.FileDeletionFailedLog;
import ecsimsw.picup.domain.FileDeletionFailedLogRepository;
import ecsimsw.picup.domain.FileResource;
import ecsimsw.picup.domain.FileResourceRepository;
import ecsimsw.picup.service.FileResourceService;
import ecsimsw.picup.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileDeletionService {

    public static final int FILE_DELETION_RETRY_COUNTS = 5;

    private final StorageService storageService;
    private final FileResourceService resourceService;
    private final FileResourceRepository fileResourceRepository;
    private final FileDeletionFailedLogRepository fileDeletionFailedLogRepository;

    @Retryable(
        maxAttempts = FILE_DELETION_RETRY_COUNTS,
        recover = "fileDeletionRecover",
        backoff = @Backoff(delayExpression = "${batch.retry.backoff.ms:500}")
    )
    public void delete(FileResource resource) {
        resourceService.deleteFromStorage(resource);
    }

    @Recover
    private void fileDeletionRecover(Exception e, FileResource resource) {
        var path = resourceService.filePath(resource);
        if (storageService.hasContent(path)) {
            var failedLog = FileDeletionFailedLog.from(resource);
            fileDeletionFailedLogRepository.save(failedLog);
            log.error("Failed to delete file resource : " + path);
        }
        fileResourceRepository.delete(resource);
    }
}