package ecsimsw.picup.album.service;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.domain.FileDeletionEventRepository;
import ecsimsw.picup.album.domain.FileDeletionEvent_;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.storage.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static ecsimsw.picup.config.S3Config.BUCKET_NAME;
import static ecsimsw.picup.config.S3Config.ROOT_PATH;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileService {

    private final AmazonS3 s3Client;
    private final ThumbnailService thumbnailService;
    private final FileDeletionEventRepository fileDeletionEventRepository;

    @Async
    public CompletableFuture<FileUploadResponse> uploadFileAsync(MultipartFile file) {
        var resourceKey = ResourceKey.generate(file);
        var uploadResponse = uploadWithLog(file, resourceKey);
        return new AsyncResult<>(uploadResponse).completable();
    }

    @Async
    public CompletableFuture<FileUploadResponse> uploadImageThumbnailAsync(MultipartFile file, float scale) {
        var resourceKey = ResourceKey.generate(file);
        var thumbnailFile = thumbnailService.resizeImage(file, scale);
        var uploadResponse = upload(thumbnailFile, resourceKey);
        return new AsyncResult<>(uploadResponse).completable();
    }

    @Async
    public CompletableFuture<FileUploadResponse> uploadVideoThumbnailAsync(MultipartFile videoFile) {
        var thumbnailFile = thumbnailService.captureVideo(videoFile);
        return uploadFileAsync(thumbnailFile);
    }

    private FileUploadResponse upload(MultipartFile file, ResourceKey resourceKey) {
        var resourcePath = ROOT_PATH + resourceKey.value();
        S3Utils.store(s3Client, BUCKET_NAME, resourcePath, file);
        return new FileUploadResponse(resourceKey, file.getSize());
    }

    private FileUploadResponse uploadWithLog(MultipartFile file, ResourceKey resourceKey) {
        var resourcePath = ROOT_PATH + resourceKey.value();
        S3Utils.storeWithLog(s3Client, BUCKET_NAME, resourcePath, file);
        return new FileUploadResponse(resourceKey, file.getSize());
    }

    public void deleteAsync(ResourceKey resourceKey) {
        fileDeletionEventRepository.save(new FileDeletionEvent(resourceKey));
    }

    public List<FileDeletionEvent> findAllDeletionEvents() {
        return fileDeletionEventRepository.findAll(Sort.by(FileDeletionEvent_.CREATION_TIME));
    }

    public void deleteAll(List<FileDeletionEvent> events) {
        events.forEach(event -> {
            try {
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
