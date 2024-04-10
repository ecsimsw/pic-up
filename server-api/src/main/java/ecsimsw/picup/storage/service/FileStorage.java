package ecsimsw.picup.storage.service;

import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.album.utils.FileUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.concurrent.CompletableFuture;

public class FileStorage implements ImageStorage {

    public final String storageKey;
    private final String rootPath;

    public FileStorage(String storageKey, String rootPath) {
        this.storageKey = storageKey;
        this.rootPath = rootPath;
    }

    @Async
    @Override
    public CompletableFuture<String> storeAsync(String resourceKey, FileUploadResponse fileUploadResponse) {
        FileUtils.writeFile(storagePath(resourceKey), fileUploadResponse.file());
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
