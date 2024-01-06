package ecsimsw.picup.service;

import ecsimsw.picup.domain.FileDeletionEvent;
import ecsimsw.picup.domain.FileDeletionEventOutbox;
import ecsimsw.picup.domain.FileExtension;
import ecsimsw.picup.dto.FileResourceInfo;
import ecsimsw.picup.exception.AlbumException;
import ecsimsw.picup.mq.ImageFileMessageQueue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
public class FileService {

    private final StorageHttpClient storageHttpClient;
    private final FileDeletionEventOutbox fileDeletionEventOutbox;
    private final ImageFileMessageQueue imageFileMessageQueue;

    public FileService(
        StorageHttpClient storageHttpClient,
        ImageFileMessageQueue imageFileMessageQueue,
        FileDeletionEventOutbox fileDeletionEventOutbox
    ) {
        this.storageHttpClient = storageHttpClient;
        this.imageFileMessageQueue = imageFileMessageQueue;
        this.fileDeletionEventOutbox = fileDeletionEventOutbox;
    }

    @Transactional
    public FileResourceInfo upload(Long userId, MultipartFile file) {
        return upload(userId, file, userId.toString());
    }

    @Transactional
    public FileResourceInfo upload(Long userId, MultipartFile file, String tag) {
        final String fileName = file.getOriginalFilename();
        if (Objects.isNull(fileName) || !fileName.contains(".")) {
            throw new AlbumException("Invalid file name");
        }
        FileExtension.fromFileName(fileName);
        return storageHttpClient.requestUpload(userId, file, tag);
    }

    @Transactional
    public void createDeleteEvent(FileDeletionEvent event) {
        fileDeletionEventOutbox.save(event);
    }

    @Transactional
    public void createDeleteEvents(List<FileDeletionEvent> events) {
        events.forEach(this::createDeleteEvent);
    }

    public void delete(String resourceKey) {
        imageFileMessageQueue.offerDeleteAllRequest(List.of(resourceKey));
    }
}
