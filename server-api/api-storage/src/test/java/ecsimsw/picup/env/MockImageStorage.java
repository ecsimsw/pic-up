package ecsimsw.picup.env;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.Resource;
import ecsimsw.picup.storage.StorageKey;
import ecsimsw.picup.service.ImageStorage;
import java.io.FileNotFoundException;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class MockImageStorage implements ImageStorage {

    private static final Map<String, byte[]> DATA = new ConcurrentHashMap<>();

    private final StorageKey KEY;

    public MockImageStorage(StorageKey key) {
        this.KEY = key;
    }

    @Override
    public CompletableFuture<Resource> storeAsync(Resource resource, ImageFile imageFile) {
        if (imageFile == null) {
            return new AsyncResult<>(resource).completable();
        }
        DATA.put(resource.getResourceKey(), imageFile.file());
        return new AsyncResult<>(resource).completable();
    }

    @Override
    public ImageFile read(Resource resource) throws FileNotFoundException {
        return ImageFile.of(resource.getResourceKey(), DATA.get(resource.getResourceKey()));
    }

    @Override
    public void deleteIfExists(Resource resource) {
        DATA.remove(resource.getResourceKey());
    }
}
