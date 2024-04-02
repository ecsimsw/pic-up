package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.domain.FileDeletionEventOutbox;
import ecsimsw.picup.album.domain.FileDeletionEvent_;
import ecsimsw.picup.album.domain.FileExtension;
import ecsimsw.picup.dto.FileUploadResponse;
import ecsimsw.picup.mq.ImageFileMessageQueue;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PictureFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PictureFileService.class);

    private final StorageHttpClient storageHttpClient;
    private final FileDeletionEventOutbox fileDeletionEventOutbox;
    private final ImageFileMessageQueue imageFileMessageQueue;

    public FileUploadResponse upload(Long userId, MultipartFile file, String resourceKey) {
        FileExtension.validate(file);
        return storageHttpClient.requestUpload(userId, file, resourceKey);
    }

    public void delete(String resourceKey) {
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

    public List<FileDeletionEvent> findAllDeletionOutBox() {
        return fileDeletionEventOutbox.findAll(
            Sort.by(FileDeletionEvent_.CREATION_TIME)
        );
    }

    @Transactional
    public void createDeleteEvents(List<FileDeletionEvent> events) {
        events.forEach(this::createDeleteEvent);
    }

    @Transactional
    public void createDeleteEvent(FileDeletionEvent event) {
        fileDeletionEventOutbox.save(event);
    }
}
