package ecsimsw.picup.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class FileService {

    private final StorageHttpClient storageClient;

    public FileService(StorageHttpClient storageClient) {
        this.storageClient = storageClient;
    }

    public String upload(MultipartFile file, String tag) {
        var response = storageClient.requestUpload(file, tag);
        return response.getResourceKey();
    }

    public void delete(String resourceKey) {
        deleteAll(List.of(resourceKey));
    }

    public void deleteAll(List<String> resources) {
        storageClient.requestDelete(resources);
    }
}
