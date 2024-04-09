package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.StoredFile;
import ecsimsw.picup.album.dto.FileReadResponse;
import ecsimsw.picup.album.exception.InvalidResourceException;
import ecsimsw.picup.album.exception.StorageException;
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
public class StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    private final ImageStorage mainStorage;
    private final ImageStorage backUpStorage;
    private final VideoThumbnailService thumbnailService;

    public StorageService(
        @Qualifier(value = "mainStorage") ImageStorage mainStorage,
        @Qualifier(value = "backUpStorage") ImageStorage backUpStorage,
        VideoThumbnailService thumbnailService
    ) {
        this.mainStorage = mainStorage;
        this.backUpStorage = backUpStorage;
        this.thumbnailService = thumbnailService;
    }

    public StoredFile upload(MultipartFile file, String resourceKey) {
        LOGGER.info("upload file : " + resourceKey);
        var storedFile = StoredFile.of(file);
        var futures = List.of(
            mainStorage.storeAsync(resourceKey, storedFile),
            backUpStorage.storeAsync(resourceKey, storedFile)
        );
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(UPLOAD_TIME_OUT_SEC, TimeUnit.SECONDS)
                .join();
            return storedFile;
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

    public FileReadResponse read(String resourceKey) {
        try {
            var file = mainStorage.read(resourceKey);
            return new FileReadResponse(resourceKey, file.byteArray(), file.size(), file.extension());
        } catch (StorageException notInMainStorage) {
            try {
                var file = backUpStorage.read(resourceKey);
                return new FileReadResponse(resourceKey, file.byteArray(), file.size(), file.extension());
            } catch (StorageException notInBackUpStorage) {
                throw new InvalidResourceException("Not exists resources : " + resourceKey);
            }
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
