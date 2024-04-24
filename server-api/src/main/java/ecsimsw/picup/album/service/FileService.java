package ecsimsw.picup.album.service;

import com.amazonaws.services.s3.AmazonS3;
import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.domain.FileDeletionEventOutbox;
import ecsimsw.picup.album.domain.FileDeletionEvent_;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.mq.ImageFileMessageQueue;
import ecsimsw.picup.storage.FileUploadResponse;
import ecsimsw.picup.storage.VideoFileUploadResponse;
import ecsimsw.picup.storage.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static ecsimsw.picup.config.S3Config.BUCKET_NAME;
import static ecsimsw.picup.config.S3Config.ROOT_PATH;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileService {

    private final AmazonS3 s3Client;
    private final ThumbnailService thumbnailService;
    private final FileDeletionEventOutbox fileDeletionEventOutbox;
    private final ImageFileMessageQueue imageFileMessageQueue;

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
        var resourcePath = ROOT_PATH + resourceKey;
        S3Utils.store(s3Client, BUCKET_NAME, resourcePath, file);
        log.info(BUCKET_NAME + " " + resourcePath);
        return new FileUploadResponse(resourceKey, file.getSize());
    }

    public void deleteAsync(ResourceKey resourceKey) {
        imageFileMessageQueue.offerDeleteAllRequest(List.of(resourceKey.getResourceKey()));
    }

    public void delete(String resourceKey) {
        S3Utils.deleteIfExists(s3Client, BUCKET_NAME, ROOT_PATH + resourceKey);
    }

    @Transactional
    public void publishDeletionEvents(List<FileDeletionEvent> events) {
        var resourceKeys = events.stream()
            .map(event -> event.getResourceKey().getResourceKey())
            .collect(Collectors.toList());
        imageFileMessageQueue.offerDeleteAllRequest(resourceKeys);
        fileDeletionEventOutbox.deleteAll(events);
        log.info("publish deletion event : " + String.join(", ", resourceKeys));
    }

    public List<FileDeletionEvent> findAllDeletionOutBox() {
        return fileDeletionEventOutbox.findAll(Sort.by(FileDeletionEvent_.CREATION_TIME));
    }

    public void createDeletionEvent(FileDeletionEvent event) {
        fileDeletionEventOutbox.save(event);
    }
}
