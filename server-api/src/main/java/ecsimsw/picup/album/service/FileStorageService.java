package ecsimsw.picup.album.service;

import ecsimsw.picup.album.exception.StorageException;
import ecsimsw.picup.storage.dto.FileUploadResponse;
import ecsimsw.picup.storage.service.FileStorage;
import ecsimsw.picup.storage.service.ObjectStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileStorageService {

    private final ObjectStorage s3Storage;
    private final FileStorage fileStorage;

    public FileUploadResponse upload(MultipartFile file, String resourceKey) {
        try {
            s3Storage.store(resourceKey, file);
            fileStorage.store(resourceKey, file);
            return new FileUploadResponse(resourceKey, file.getSize());
        } catch (Exception e) {
            s3Storage.deleteIfExists(resourceKey);
            fileStorage.deleteIfExists(resourceKey);
            throw new StorageException("exception while uploading : " + e.getMessage());
        }
    }

    public void delete(String resourceKey) {
        try {
            s3Storage.deleteIfExists(resourceKey);
        } catch (Exception ignored) {
            log.error("Failed to delete resource from main storage: " + resourceKey);
        }
        try {
            fileStorage.deleteIfExists(resourceKey);
        } catch (Exception ignored) {
            log.error("Failed to delete resource from backUp storage: " + resourceKey);
        }
    }
}
