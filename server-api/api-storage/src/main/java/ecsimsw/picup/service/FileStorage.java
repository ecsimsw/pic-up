package ecsimsw.picup.service;

import ecsimsw.picup.domain.StoredFile;
import ecsimsw.picup.utils.FileUtils;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

public class FileStorage implements ImageStorage {

    public final String storageKey;
    private final String rootPath;

    public FileStorage(String storageKey, String rootPath) {
        this.storageKey = storageKey;
        this.rootPath = rootPath;
    }

    @Async
    @Override
    public CompletableFuture<String> storeAsync(String resourceKey, StoredFile storedFile) {
        FileUtils.writeFile(storagePath(resourceKey), storedFile.file());
        return new AsyncResult<>(resourceKey).completable();
    }

    @Override
    public StoredFile read(String resourceKey) {
        var storagePath = storagePath(resourceKey);
        var file = FileUtils.read(storagePath);
        return StoredFile.of(resourceKey, file);
    }

    @Override
    public void deleteIfExists(String resourceKey) {
        FileUtils.deleteIfExists(storagePath(resourceKey));
    }

    private String storagePath(String resourceKey) {
        return rootPath + resourceKey;
    }
}
