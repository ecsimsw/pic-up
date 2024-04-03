package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
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
    public CompletableFuture<Resource> storeAsync(Resource resource, ImageFile imageFile) {
        FileUtils.writeFile(storagePath(resource.getResourceKey()), imageFile.file());
        resource.storedTo(storageKey);
        return new AsyncResult<>(resource).completable();
    }

    @Override
    public ImageFile read(Resource resource) throws FileNotFoundException {
        var resourceKey = resource.getResourceKey();
        var storagePath = storagePath(resourceKey);
        var file = FileUtils.read(storagePath);
        return ImageFile.of(resourceKey, file);
    }

    @Override
    public void deleteIfExists(Resource resource) {
        FileUtils.deleteIfExists(storagePath(resource.getResourceKey()));
        resource.deletedFrom(storageKey);
    }

    private String storagePath(String resourceKey) {
        return rootPath + resourceKey;
    }
}
