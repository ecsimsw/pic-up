package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.domain.FileDeletionEventOutbox;
import ecsimsw.picup.album.domain.FileExtension;
import ecsimsw.picup.album.dto.FileResourceInfo;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.mq.ImageFileMessageQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class FileService {

    private final StorageHttpClient storageHttpClient;
    private final FileDeletionEventOutbox fileDeletionEventOutbox;
    private final ImageFileMessageQueue imageFileMessageQueue;



    public FileResourceInfo upload(Long userId, MultipartFile file) {
        return upload(userId, file, userId.toString());
    }

    public FileResourceInfo upload(Long userId, MultipartFile file, String tag) {
        var fileName = file.getOriginalFilename();
        if (Objects.isNull(fileName) || !fileName.contains(".")) {
            throw new AlbumException("Invalid file name");
        }
        FileExtension.fromFileName(fileName);
        return storageHttpClient.requestUpload(userId, file, tag);
    }

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
