package ecsimsw.picup.service;

import ecsimsw.picup.exception.FileException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
public class FileService {

    private final StorageHttpClient storageClient;

    public FileService(StorageHttpClient storageClient) {
        this.storageClient = storageClient;
    }

    public String upload(MultipartFile file, String tag) {
        var response = storageClient.requestUpload(file, tag);
        if (Objects.isNull(response.getResourceKey())) {
            throw new FileException("Failed to upload resources.\nStorage server is on, but resource key is not found.");
        }
        return response.getResourceKey();
    }

    public void delete(String resourceKey) {
        deleteAll(List.of(resourceKey));
    }

    public void deleteAll(List<String> resources) {
        storageClient.requestDelete(resources);
    }
}
