package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.domain.FileDeletionEventOutbox;
import ecsimsw.picup.album.domain.FileDeletionEvent_;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.mq.ImageFileMessageQueue;
import ecsimsw.picup.storage.dto.FileUploadResponse;
import ecsimsw.picup.storage.dto.VideoFileUploadResponse;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class FileService {

    private final FileStorageService storageService;
    private final ThumbnailService thumbnailService;
    private final FileDeletionEventOutbox fileDeletionEventOutbox;
    private final ImageFileMessageQueue imageFileMessageQueue;

    @Async
    public CompletableFuture<FileUploadResponse> uploadImageAsync(MultipartFile file) {
        var resourceKey = ResourceKey.generate(file);
        var uploadResponse = storageService.upload(file, resourceKey);
        return new AsyncResult<>(uploadResponse).completable();
    }

    public VideoFileUploadResponse uploadVideo(MultipartFile videoFile) {
        var videoResourceKey = ResourceKey.generate(videoFile);
        var videoFileInfo = storageService.upload(videoFile, videoResourceKey);
        var thumbnailFile = thumbnailService.videoThumbnail(videoResourceKey);
        var thumbnailFileInfo = storageService.upload(thumbnailFile);
        return new VideoFileUploadResponse(
            videoFileInfo.resourceKey(),
            thumbnailFileInfo.resourceKey(),
            videoFileInfo.size()
        );
    }

    public void deleteAsync(ResourceKey resourceKey) {
        imageFileMessageQueue.offerDeleteAllRequest(List.of(resourceKey.getResourceKey()));
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

    @Transactional(readOnly = true)
    public List<FileDeletionEvent> findAllDeletionOutBox() {
        return fileDeletionEventOutbox.findAll(Sort.by(FileDeletionEvent_.CREATION_TIME));
    }

    @Transactional
    public void createDeletionEvent(FileDeletionEvent event) {
        fileDeletionEventOutbox.save(event);
    }
}
