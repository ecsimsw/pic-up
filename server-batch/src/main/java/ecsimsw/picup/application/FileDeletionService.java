package ecsimsw.picup.application;

import ecsimsw.picup.domain.FileDeletionFailedLog;
import ecsimsw.picup.domain.FileDeletionFailedLogRepository;
import ecsimsw.picup.storage.domain.FileResource;
import ecsimsw.picup.storage.domain.FileResourceRepository;
import ecsimsw.picup.storage.service.FileResourceService;
import ecsimsw.picup.storage.service.FileStorageService;
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

    private final FileStorageService fileStorageService;
    private final FileResourceService resourceService;
    private final FileResourceRepository fileResourceRepository;
    private final FileDeletionFailedLogRepository fileDeletionFailedLogRepository;

    @Retryable(
        maxAttempts = FILE_DELETION_RETRY_COUNTS,
        recover = "fileDeletionRecover",
        backoff = @Backoff(delayExpression = "${batch.retry.backoff.ms:500}")
    )
    public void delete(FileResource resource) {
        var path = resourceService.filePath(resource);
        fileStorageService.delete(path);
        fileResourceRepository.delete(resource);
    }

    @Recover
    private void fileDeletionRecover(Exception e, FileResource resource) {
        var path = resourceService.filePath(resource);
        if (fileStorageService.hasContent(path)) {
            var failedLog = FileDeletionFailedLog.from(resource);
            fileDeletionFailedLogRepository.save(failedLog);
            log.error("Failed to delete file resource : " + path);
        }
        fileResourceRepository.delete(resource);
    }
}
