package ecsimsw.picup.storage.service;

import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.storage.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.concurrent.CompletableFuture;

public class FileStorage implements ImageStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorage.class);

    private final String rootPath;

    public FileStorage(String rootPath) {
        this.rootPath = rootPath;
    }

    @Async
    @Override
    public CompletableFuture<String> storeAsync(String resourceKey, FileUploadResponse fileUploadResponse) {
        var start = System.currentTimeMillis();
        FileUtils.writeFile(storagePath(resourceKey), fileUploadResponse.file());
        LOGGER.info("FS upload time " + (System.currentTimeMillis() -start) + "ms, for " + fileUploadResponse.size());
        return new AsyncResult<>(resourceKey).completable();
    }

    @Override
    public FileUploadResponse read(String resourceKey) {
        var storagePath = storagePath(resourceKey);
        var file = FileUtils.read(storagePath);
        return FileUploadResponse.of(resourceKey, file);
    }

    @Override
    public void deleteIfExists(String resourceKey) {
        FileUtils.deleteIfExists(storagePath(resourceKey));
    }

    private String storagePath(String resourceKey) {
        return rootPath + resourceKey;
    }
}
