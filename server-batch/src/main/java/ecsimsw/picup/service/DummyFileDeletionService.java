package ecsimsw.picup.service;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.domain.FileDeletionFailedLog;
import ecsimsw.picup.domain.FileDeletionFailedLogRepository;
import ecsimsw.picup.storage.domain.FileResource;
import ecsimsw.picup.storage.domain.FileResourceRepository;
import ecsimsw.picup.storage.service.FileResourceService;
import ecsimsw.picup.storage.utils.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import static ecsimsw.picup.storage.config.S3Config.BUCKET;

@Slf4j
@RequiredArgsConstructor
@Service
public class DummyFileDeletionService {

    private static final int FILE_DELETION_RETRY_COUNTS = 5;

    private final AmazonS3 amazonS3;
    private final FileResourceService resourceService;
    private final FileResourceRepository fileResourceRepository;
    private final FileDeletionFailedLogRepository fileDeletionFailedLogRepository;

    @Retryable(
        maxAttempts = FILE_DELETION_RETRY_COUNTS,
        recover = "fileDeletionRecover",
        backoff = @Backoff(delay = 500)
    )
    public void delete(FileResource resource) {
        var path = resourceService.filePath(resource);
        S3Utils.delete(amazonS3, BUCKET, path);
        fileResourceRepository.delete(resource);
    }

    @Recover
    private void fileDeletionRecover(Exception e, FileResource resource) {
        var path = resourceService.filePath(resource);
        if (S3Utils.hasContent(amazonS3, BUCKET, path)) {
            var failedLog = FileDeletionFailedLog.from(resource);
            fileDeletionFailedLogRepository.save(failedLog);
            log.error("Failed to delete file resource : " + path);
        }
        fileResourceRepository.delete(resource);
    }
}
