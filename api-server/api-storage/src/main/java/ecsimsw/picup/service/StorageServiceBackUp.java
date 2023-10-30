package ecsimsw.picup.service;

import ecsimsw.picup.dto.ImageResponse;
import ecsimsw.picup.dto.ImageUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class StorageServiceBackUp {

    private final StorageService storageService;

    public StorageServiceBackUp(StorageService storageService) {
        this.storageService = storageService;
    }

    public ImageUploadResponse upload(MultipartFile file, String tag) {
        return storageService.upload(tag, file);
    }

    public ImageResponse read(String resourceKey) {
        return storageService.read(resourceKey);
    }

    public void delete(String resourceKey) {
        storageService.delete(resourceKey);
    }

    public void deleteAll(List<String> resourceKeys) {
        resourceKeys.forEach(this::delete);
    }
}
