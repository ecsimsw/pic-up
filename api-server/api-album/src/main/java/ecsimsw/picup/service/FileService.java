package ecsimsw.picup.service;

import ecsimsw.picup.domain.FileExtension;
import ecsimsw.picup.exception.AlbumException;
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
        validateFileType(file.getOriginalFilename());
        var response = storageClient.requestUpload(file, tag);
        return response.getResourceKey();
    }

    public void delete(String resourceKey) {
        validateFileType(resourceKey);
        deleteAll(List.of(resourceKey));
    }

    public void deleteAll(List<String> resources) {
        resources.forEach(this::validateFileType);
        storageClient.requestDelete(resources);
    }

    private void validateFileType(String fileName) {
        if(Objects.isNull(fileName) || !fileName.contains(".")) {
            throw new AlbumException("Invalid file type");
        }
        FileExtension.fromFileName(fileName);
    }
}