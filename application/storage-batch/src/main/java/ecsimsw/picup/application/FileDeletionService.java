package ecsimsw.picup.application;

import ecsimsw.picup.domain.FileDeletionFailedLog;
import ecsimsw.picup.domain.FileDeletionFailedLogRepository;
import ecsimsw.picup.domain.FileResource;
import ecsimsw.picup.domain.FileResourceRepository;
import ecsimsw.picup.service.FileStorage;
import ecsimsw.picup.service.ResourceService;
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

    private final FileStorage fileStorage;
    private final ResourceService resourceService;
    private final FileResourceRepository fileResourceRepository;
    private final FileDeletionFailedLogRepository fileDeletionFailedLogRepository;

    @Retryable(
        maxAttempts = FILE_DELETION_RETRY_COUNTS,
        recover = "fileDeletionRecover",
        backoff = @Backoff(delayExpression = "${batch.retry.backoff.ms:500}")
    )
    public void delete(FileResource resource) {
        var path = resourceService.filePath(resource);
        fileStorage.delete(path);
        fileResourceRepository.delete(resource);
        log.info("delete : " + resource.getResourceKey() + ", " + resource.getCreatedAt().toString());
    }

    @Recover
    private void fileDeletionRecover(Exception e, FileResource resource) {
        try {
            var path = resourceService.filePath(resource);
            if (fileStorage.hasContent(path)) {
                var failedLog = FileDeletionFailedLog.from(resource);
                fileDeletionFailedLogRepository.save(failedLog);
                log.error("Failed to delete file resource : " + path);
            }
            fileResourceRepository.delete(resource);
        } catch (Exception unableToCheckS3Exception) {
            var failedLog = FileDeletionFailedLog.from(resource);
            fileDeletionFailedLogRepository.save(failedLog);
            fileResourceRepository.delete(resource);
        }
    }
}
