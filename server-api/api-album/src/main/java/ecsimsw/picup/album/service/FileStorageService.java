package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.domain.FileDeletionEventOutbox;
import ecsimsw.picup.album.domain.FileDeletionEvent_;
import ecsimsw.picup.album.domain.PictureFile;
import ecsimsw.picup.dto.FileReadResponse;
import ecsimsw.picup.dto.ImageFileUploadRequest;
import ecsimsw.picup.dto.ImageFileUploadResponse;
import ecsimsw.picup.dto.VideoFileUploadRequest;
import ecsimsw.picup.dto.VideoFileUploadResponse;
import ecsimsw.picup.mq.ImageFileMessageQueue;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FileStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorageService.class);

    private final StorageHttpClient storageHttpClient;
    private final FileDeletionEventOutbox fileDeletionEventOutbox;
    private final ImageFileMessageQueue imageFileMessageQueue;

    public ImageFileUploadResponse uploadImage(PictureFile file) {
        var request = new ImageFileUploadRequest(file.toMultipartFile(), file.resourceKey());
        return storageHttpClient.requestUploadImage(request);
    }

    public VideoFileUploadResponse uploadVideo(PictureFile file) {
        var request = new VideoFileUploadRequest(file.toMultipartFile(), file.resourceKey());
        return storageHttpClient.requestUploadVideo(request);
    }

    public FileReadResponse read(String resourceKey) {
        return storageHttpClient.requestReadFile(resourceKey);
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
