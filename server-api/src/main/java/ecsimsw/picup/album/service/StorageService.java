package ecsimsw.picup.album.service;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.album.exception.AlbumException;
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
public class StorageService {

    public static final String ROOT_PATH = "storage/";
    public static final String THUMBNAIL_PATH = "thumb/";
    public static final String BUCKET_NAME = "picup-ecsimsw";

    private static final long PRE_SIGNED_URL_EXPIRATION_MS = 10_000;
    private static final int WAIT_TIME_FOR_PICTURE_UPLOAD_SEC = 10;
    private static final int FILE_DELETION_RETRY_COUNTS = 3;

    private final AmazonS3 s3Client;
    private final ThumbnailService thumbnailService;
    private final PreUploadPictureRepository preUploadPictureRepository;
    private final FileDeletionEventRepository fileDeletionEventRepository;

    @Async
    public CompletableFuture<FileUploadResponse> uploadImageThumbnailAsync(MultipartFile file, float scale) {
        var thumbnailFile = thumbnailService.resizeImage(file, scale);
        var resourceKey = ResourceKey.fromMultipartFile(thumbnailFile);
        S3Utils.store(s3Client, BUCKET_NAME, THUMBNAIL_PATH + resourceKey.value(), file);
        var uploadResponse = new FileUploadResponse(resourceKey, file.getSize());
        return new AsyncResult<>(uploadResponse).completable();
    }

    @Transactional
    public String preSingedUrl(PreUploadPicture preUploadPicture) {
        return S3Utils.getPreSignedUrl(
            s3Client,
            BUCKET_NAME,
            ROOT_PATH + preUploadPicture.getResourceKey(),
            PRE_SIGNED_URL_EXPIRATION_MS
        );
    }

    @Transactional
    public PreUploadPicture commit(String resourceKey) {
        var preUploadEvent = preUploadPictureRepository.findById(resourceKey).orElseThrow(() -> new AlbumException("Nothing to commit"));
        preUploadPictureRepository.delete(preUploadEvent);
        return preUploadEvent;
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
        preUploadPictureRepository.deleteAllCreatedAfter(expiration);
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
                var resourcePath = ROOT_PATH + event.getResourceKey().value();;
                S3Utils.deleteIfExists(s3Client, BUCKET_NAME, ROOT_PATH + resourcePath);
                S3Utils.deleteIfExists(s3Client, BUCKET_NAME, THUMBNAIL_PATH + resourcePath);
                fileDeletionEventRepository.delete(event);
            } catch (Exception e) {
                event.countFailed();
                fileDeletionEventRepository.save(event);
            }
        });
    }
}
