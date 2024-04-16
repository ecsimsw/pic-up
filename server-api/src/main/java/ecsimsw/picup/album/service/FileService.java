package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.domain.FileDeletionEventOutbox;
import ecsimsw.picup.album.domain.FileDeletionEvent_;
import ecsimsw.picup.album.dto.FileUploadRequest;
import ecsimsw.picup.mq.ImageFileMessageQueue;
import ecsimsw.picup.storage.dto.FileUploadResponse;
import ecsimsw.picup.storage.dto.VideoFileUploadResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    private final FileStorageService storageService;
    private final VideoThumbnailService thumbnailService;
    private final FileDeletionEventOutbox fileDeletionEventOutbox;
    private final ImageFileMessageQueue imageFileMessageQueue;

    @Async
    public CompletableFuture<FileUploadResponse> uploadImageAsync(FileUploadRequest file) {
        var uploadResponse = storageService.upload(file.toMultipartFile(), file.resourceKey());
        return new AsyncResult<>(uploadResponse).completable();
    }

    public VideoFileUploadResponse uploadVideo(FileUploadRequest file) {
        var videoInfo = storageService.upload(file.toMultipartFile(), file.resourceKey());
        var thumbnailInfo = thumbnailService.videoThumbnail(videoInfo.resourceKey());
        storageService.upload(thumbnailInfo.toMultipartFile(), thumbnailInfo.resourceKey());
        return new VideoFileUploadResponse(
            videoInfo.resourceKey(),
            thumbnailInfo.resourceKey(),
            videoInfo.size()
        );
    }

    public void deleteAsync(String resourceKey) {
        imageFileMessageQueue.offerDeleteAllRequest(List.of(resourceKey));
    }

    @Transactional
    public void publishDeletionEvents(List<FileDeletionEvent> events) {
        var resourceKeys = events.stream()
            .map(FileDeletionEvent::getResourceKey)
            .collect(Collectors.toList());
        imageFileMessageQueue.offerDeleteAllRequest(resourceKeys);
        fileDeletionEventOutbox.deleteAll(events);
        LOGGER.info("publish deletion event : " + String.join(", ", resourceKeys));
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
