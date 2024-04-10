package ecsimsw.picup.album.service;

import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.album.exception.StorageException;
import ecsimsw.picup.storage.dto.ImageFileUploadResponse;
import ecsimsw.picup.storage.service.ImageStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import static ecsimsw.picup.album.config.FileStorageConfig.UPLOAD_TIME_OUT_SEC;

@Service
public class FileStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorageService.class);

    private final ImageStorage mainStorage;
    private final ImageStorage backUpStorage;

    public FileStorageService(
        @Qualifier(value = "mainStorage") ImageStorage mainStorage,
        @Qualifier(value = "backUpStorage") ImageStorage backUpStorage
    ) {
        this.mainStorage = mainStorage;
        this.backUpStorage = backUpStorage;
    }

    public ImageFileUploadResponse upload(MultipartFile file, String resourceKey) {
        LOGGER.info("upload file : " + resourceKey);
        var storedFile = FileUploadResponse.of(resourceKey, file);
        var futures = List.of(
            mainStorage.storeAsync(resourceKey, storedFile),
            backUpStorage.storeAsync(resourceKey, storedFile)
        );
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(UPLOAD_TIME_OUT_SEC, TimeUnit.SECONDS)
                .join();
            return new ImageFileUploadResponse(resourceKey, file.getSize());
        } catch (CompletionException e) {
            futures.forEach(uploadFuture -> uploadFuture.thenAccept(
                uploadResponse -> {
                    mainStorage.deleteIfExists(resourceKey);
                    backUpStorage.deleteIfExists(resourceKey);
                })
            );
            throw new StorageException("exception while uploading : " + e.getMessage());
        }
    }

    public void delete(String resourceKey) {
        try {
            mainStorage.deleteIfExists(resourceKey);
        } catch (Exception ignored) {
            LOGGER.error("Failed to delete resource from main storage: " + resourceKey);
        }
        try {
            backUpStorage.deleteIfExists(resourceKey);
        } catch (Exception ignored) {
            LOGGER.error("Failed to delete resource from backUp storage: " + resourceKey);
        }
    }
}
