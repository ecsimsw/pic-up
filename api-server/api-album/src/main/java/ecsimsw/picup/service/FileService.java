package ecsimsw.picup.service;

import com.google.common.collect.Iterables;
import ecsimsw.picup.domain.FileExtension;
import ecsimsw.picup.exception.AlbumException;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private final StorageHttpClient storageHttpClient;
    private final StorageMessageQueue storageMessageQueue;

    public FileService(
        StorageHttpClient storageHttpClient,
        StorageMessageQueue storageMessageQueue
    ) {
        this.storageHttpClient = storageHttpClient;
        this.storageMessageQueue = storageMessageQueue;
    }

    public String upload(MultipartFile file, String tag) {
        validateFileType(file.getOriginalFilename());
        var response = storageHttpClient.requestUpload(file, tag);
        return response.getResourceKey();
    }

    public void delete(String resourceKey) {
        validateFileType(resourceKey);
        deleteAll(List.of(resourceKey));
    }

    public void deleteAll(List<String> resources) {
        resources.forEach(this::validateFileType);
        for(var resourcePart : Iterables.partition(resources, 5)) {
            storageMessageQueue.pollDeleteRequest(resourcePart);
        }
    }

    private void validateFileType(String fileName) {
        if (Objects.isNull(fileName) || !fileName.contains(".")) {
            throw new AlbumException("Invalid file type");
        }
        FileExtension.fromFileName(fileName);
    }
}
