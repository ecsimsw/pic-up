package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.VideoThumbnailFile;
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

    public FileUploadResponse upload(MultipartFile file, ResourceKey resourceKey) {
        var resourceName = resourceKey.getResourceKey();
        try {
            s3Storage.store(resourceName, file);
            fileStorage.store(resourceName, file);
            return new FileUploadResponse(resourceKey, file.getSize());
        } catch (Exception e) {
            e.printStackTrace();
            s3Storage.deleteIfExists(resourceName);
            fileStorage.deleteIfExists(resourceName);
            throw new StorageException("exception while uploading : " + e.getMessage());
        }
    }

    public FileUploadResponse upload(VideoThumbnailFile thumbnailFile) {
        return upload(thumbnailFile.file(), thumbnailFile.resourceKey());
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
