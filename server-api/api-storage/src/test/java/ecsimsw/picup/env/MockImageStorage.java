package ecsimsw.picup.env;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.storage.StorageKey;
import ecsimsw.picup.dto.StorageUploadResponse;
import ecsimsw.picup.storage.ImageStorage;
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
    public CompletableFuture<StorageUploadResponse> storeAsync(String resourceKey, ImageFile imageFile) {
        if (imageFile == null) {
            return new AsyncResult<>(new StorageUploadResponse(resourceKey, KEY, 0)).completable();
        }
        DATA.put(resourceKey, imageFile.file());
        return new AsyncResult<>(new StorageUploadResponse(resourceKey, KEY, imageFile.size())).completable();
    }

    @Override
    public ImageFile read(String resourceKey) {
        return ImageFile.of(resourceKey, DATA.get(resourceKey));
    }

    @Override
    public void delete(String resourceKey) {
        DATA.remove(resourceKey);
    }

    @Override
    public StorageKey key() {
        return KEY;
    }
}
