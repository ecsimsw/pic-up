package ecsimsw.picup.service;

import com.google.common.collect.Iterables;
import ecsimsw.picup.domain.FileExtension;
import ecsimsw.picup.dto.ImageFileInfo;
import ecsimsw.picup.exception.AlbumException;
import java.util.List;
import java.util.Objects;

import ecsimsw.picup.exception.MessageQueueServerDownException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    public final static int FILE_DELETION_SEGMENT_UNIT = 5;

    private final StorageHttpClient storageHttpClient;
    private final StorageMessageQueue storageMessageQueue;

    public FileService(
        StorageHttpClient storageHttpClient,
        StorageMessageQueue storageMessageQueue
    ) {
        this.storageHttpClient = storageHttpClient;
        this.storageMessageQueue = storageMessageQueue;
    }

    public ImageFileInfo upload(MultipartFile file, Long tag) {
        return upload(file, tag.toString());
    }

    public ImageFileInfo upload(MultipartFile file, String tag) {
        final String fileName = file.getOriginalFilename();
        if (Objects.isNull(fileName) || !fileName.contains(".")) {
            throw new AlbumException("Invalid file name");
        }
        FileExtension.fromFileName(fileName);
        return storageHttpClient.requestUpload(file, tag);
    }

    public void delete(String resourceKey) {
        storageMessageQueue.pollDeleteRequest(List.of(resourceKey));
    }

    public void deleteAll(List<String> resources) {
        for(var resourcePart : Iterables.partition(resources, FILE_DELETION_SEGMENT_UNIT)) {
            storageMessageQueue.pollDeleteRequest(resourcePart);
        }
    }
}
