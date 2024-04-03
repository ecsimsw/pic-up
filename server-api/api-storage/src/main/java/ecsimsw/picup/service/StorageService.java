package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.dto.FileReadResponse;
import ecsimsw.picup.dto.FileUploadResponse;
import ecsimsw.picup.exception.InvalidResourceException;
import ecsimsw.picup.exception.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import static ecsimsw.picup.config.FileStorageConfig.UPLOAD_TIME_OUT_SEC;

@Service
public class StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    private final ImageStorage mainStorage;
    private final ImageStorage backUpStorage;

    public StorageService(
        @Qualifier(value = "mainStorage") ImageStorage mainStorage,
        @Qualifier(value = "backUpStorage") ImageStorage backUpStorage
    ) {
        this.mainStorage = mainStorage;
        this.backUpStorage = backUpStorage;
    }

    @Transactional
    public FileUploadResponse upload(MultipartFile file, String resourceKey) {
        LOGGER.info("upload file : " + resourceKey);
        var imageFile = ImageFile.of(file);
        var futures = List.of(
            mainStorage.storeAsync(resourceKey, imageFile),
            backUpStorage.storeAsync(resourceKey, imageFile)
        );
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(UPLOAD_TIME_OUT_SEC, TimeUnit.SECONDS)
                .join();
        } catch (CompletionException e) {
            futures.forEach(uploadFuture -> uploadFuture.thenAccept(
                uploadResponse -> {
                    mainStorage.deleteIfExists(resourceKey);
                    backUpStorage.deleteIfExists(resourceKey);
                })
            );
            throw new StorageException("exception while uploading : " + e.getMessage());
        }
        return new FileUploadResponse(resourceKey, imageFile.size());
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
