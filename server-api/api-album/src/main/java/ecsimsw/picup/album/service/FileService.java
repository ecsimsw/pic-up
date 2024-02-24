package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.domain.FileDeletionEventOutbox;
import ecsimsw.picup.album.domain.FileDeletionEvent_;
import ecsimsw.picup.album.domain.FileExtension;
import ecsimsw.picup.album.dto.FileResourceInfo;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.alert.SlackMessageSender;
import ecsimsw.picup.mq.ImageFileMessageQueue;
import ecsimsw.picup.mq.exception.MessageBrokerDownException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

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

    @Transactional
    public void publishDeletionEvents(List<FileDeletionEvent> events) {
        try {
            var resourceKeys = events.stream()
                .map(FileDeletionEvent::getResourceKey)
                .collect(Collectors.toList());
            imageFileMessageQueue.offerDeleteAllRequest(resourceKeys);
            fileDeletionEventOutbox.deleteAll(events);
            LOGGER.info("publish deletion event : " + String.join(", ", resourceKeys));
        } catch (MessageBrokerDownException e) {
            var alertMessage = "[MESSAGE_BROKER_CONNECT_] : " + e.getMessage();
            LOGGER.error(alertMessage + "\n" + e.getCause());
            SlackMessageSender.send(alertMessage);
        }
    }

    public List<FileDeletionEvent> findAllDeletionOutBox() {
        return fileDeletionEventOutbox.findAll(
            Sort.by(FileDeletionEvent_.CREATION_TIME)
        );
    }
}
