package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.storage.StorageKey;
import ecsimsw.picup.utils.FileUtils;
import java.io.FileNotFoundException;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

@Component(value = "localFileStorage")
public class LocalFileStorage implements ImageStorage {

    public static final StorageKey KEY = StorageKey.LOCAL_FILE_STORAGE;
    private static final String rootPath = "./storage/";

    @Async
    @Override
    public CompletableFuture<Resource> storeAsync(Resource resource, ImageFile imageFile) {
        FileUtils.writeFile(storagePath(resource.getResourceKey()), imageFile.file());
        resource.storedTo(KEY);
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
    public void delete(Resource resource) {
        FileUtils.deleteIfExists(storagePath(resource.getResourceKey()));
        resource.deletedFrom(KEY);
    }

    private String storagePath(String resourceKey) {
        return rootPath + resourceKey;
    }
}
