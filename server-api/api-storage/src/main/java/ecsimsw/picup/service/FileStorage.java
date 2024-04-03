package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.utils.FileUtils;
import java.io.FileNotFoundException;
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
    public CompletableFuture<String> storeAsync(String resourceKey, ImageFile imageFile) {
        FileUtils.writeFile(storagePath(resourceKey), imageFile.file());
        return new AsyncResult<>(resourceKey).completable();
    }

    @Override
    public ImageFile read(String resourceKey) throws FileNotFoundException {
        var storagePath = storagePath(resourceKey);
        var file = FileUtils.read(storagePath);
        return ImageFile.of(resourceKey, file);
    }

    @Override
    public void deleteIfExists(String resourceKey) {
        FileUtils.deleteIfExists(storagePath(resourceKey));
    }

    private String storagePath(String resourceKey) {
        return rootPath + resourceKey;
    }
}
