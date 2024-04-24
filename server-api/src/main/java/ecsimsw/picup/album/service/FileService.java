package ecsimsw.picup.album.service;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.domain.FileDeletionEventRepository;
import ecsimsw.picup.album.domain.FileDeletionEvent_;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.storage.FileUploadResponse;
import ecsimsw.picup.storage.S3Utils;
import ecsimsw.picup.storage.VideoFileUploadResponse;
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
    public CompletableFuture<FileUploadResponse> uploadImageAsync(MultipartFile file) {
        var resourceKey = ResourceKey.generate(file);
        var uploadResponse = upload(file, resourceKey);
        return new AsyncResult<>(uploadResponse).completable();
    }

    @Async
    public CompletableFuture<VideoFileUploadResponse> uploadVideoAsync(MultipartFile videoFile) {
        var videoResourceKey = ResourceKey.generate(videoFile);
        var videoFileInfo = upload(videoFile, videoResourceKey);
        var thumbnailFile = thumbnailService.videoThumbnail(videoFile);
        var thumbnailFileInfo = upload(thumbnailFile.file(), thumbnailFile.resourceKey());
        var uploadResponse = new VideoFileUploadResponse(
            videoFileInfo.resourceKey(),
            thumbnailFileInfo.resourceKey(),
            videoFileInfo.size()
        );
        return new AsyncResult<>(uploadResponse).completable();
    }

    private FileUploadResponse upload(MultipartFile file, ResourceKey resourceKey) {
        var resourcePath = ROOT_PATH + resourceKey.value();
        S3Utils.store(s3Client, BUCKET_NAME, resourcePath, file);
        log.info(BUCKET_NAME + " " + resourcePath);
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
