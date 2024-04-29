package ecsimsw.picup.album.service;

import static ecsimsw.picup.config.S3Config.BUCKET_NAME;
import static ecsimsw.picup.config.S3Config.ROOT_PATH;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.domain.FileDeletionEventRepository;
import ecsimsw.picup.album.domain.FileDeletionEvent_;
import ecsimsw.picup.album.domain.FilePreUploadEventRepository;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.storage.S3Utils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileService {

    private static final int FILE_DELETION_RETRY_COUNTS = 3;
    private static final int WAIT_TIME_FOR_PICTURE_UPLOAD_SEC = 10;

    private final AmazonS3 s3Client;
    private final ThumbnailService thumbnailService;
    private final FilePreUploadEventRepository filePreUploadEventRepository;
    private final FileDeletionEventRepository fileDeletionEventRepository;

    @Async
    public CompletableFuture<FileUploadResponse> uploadFileAsync(MultipartFile file) {
        var resourceKey = ResourceKey.generate(file);
        var resourcePath = ROOT_PATH + resourceKey.value();
        S3Utils.store(s3Client, BUCKET_NAME, resourcePath, file);
        var uploadResponse = new FileUploadResponse(resourceKey, file.getSize());
        return new AsyncResult<>(uploadResponse).completable();
    }

    @Async
    public CompletableFuture<FileUploadResponse> uploadImageThumbnailAsync(MultipartFile file, float scale) {
        var thumbnailFile = thumbnailService.resizeImage(file, scale);
        return uploadFileAsync(thumbnailFile);
    }

    @Async
    public CompletableFuture<FileUploadResponse> uploadVideoThumbnailAsync(MultipartFile videoFile) {
        var thumbnailFile = thumbnailService.captureVideo(videoFile);
        return uploadFileAsync(thumbnailFile);
    }

    @Transactional
    public void deleteAsync(ResourceKey resourceKey) {
        fileDeletionEventRepository.save(new FileDeletionEvent(resourceKey));
    }

    @Transactional(readOnly = true)
    public List<FileDeletionEvent> findAllDeletionEvents() {
        return fileDeletionEventRepository.findAll(Sort.by(FileDeletionEvent_.CREATION_TIME));
    }

    @Transactional
    public void deleteFailedToUploadFiles() {
        var expiration = LocalDateTime.now().minusSeconds(WAIT_TIME_FOR_PICTURE_UPLOAD_SEC);
        filePreUploadEventRepository.findAllByCreatedAtGreaterThan(expiration);
    }

    @Transactional
    public void deleteAll(List<FileDeletionEvent> events) {
        events.forEach(event -> {
            try {
                if (event.getDeleteFailedCounts() > FILE_DELETION_RETRY_COUNTS) {
                    fileDeletionEventRepository.delete(event);
                    log.error("failed to delete");
                    return;
                }
                var resourcePath = ROOT_PATH + event.getResourceKey().value();
                S3Utils.deleteIfExists(s3Client, BUCKET_NAME, resourcePath);
                fileDeletionEventRepository.delete(event);
            } catch (Exception e) {
                event.countFailed();
                fileDeletionEventRepository.save(event);
            }
        });
    }
}
