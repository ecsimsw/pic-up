package ecsimsw.picup.album.service;

import static ecsimsw.picup.storage.service.FileStorage.FILE_STORAGE_PATH;

import ecsimsw.picup.album.exception.StorageException;
import ecsimsw.picup.album.utils.VideoUtils;
import ecsimsw.picup.storage.dto.FileUploadResponse;
import ecsimsw.picup.storage.service.FileStorage;
import ecsimsw.picup.storage.service.ObjectStorage;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class FileStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorageService.class);

    public static final int UPLOAD_TIME_OUT_SEC = 5;

    private final ObjectStorage s3Storage;
    private final FileStorage fileStorage;

    public FileUploadResponse upload(MultipartFile file, String resourceKey) {
        LOGGER.info("upload file : " + resourceKey);
        var futures = List.of(
            s3Storage.storeAsync(resourceKey, file),
            fileStorage.storeAsync(resourceKey, file)
        );
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(UPLOAD_TIME_OUT_SEC, TimeUnit.SECONDS)
                .join();
            return new FileUploadResponse(resourceKey, file.getSize());
        } catch (CompletionException e) {
            futures.forEach(uploadFuture -> uploadFuture.thenAccept(
                uploadResponse -> {
                    s3Storage.deleteIfExists(resourceKey);
                    fileStorage.deleteIfExists(resourceKey);
                })
            );
            throw new StorageException("exception while uploading : " + e.getMessage());
        }
    }

    public void delete(String resourceKey) {
        try {
            s3Storage.deleteIfExists(resourceKey);
        } catch (Exception ignored) {
            LOGGER.error("Failed to delete resource from main storage: " + resourceKey);
        }
        try {
            fileStorage.deleteIfExists(resourceKey);
        } catch (Exception ignored) {
            LOGGER.error("Failed to delete resource from backUp storage: " + resourceKey);
        }
    }
}
