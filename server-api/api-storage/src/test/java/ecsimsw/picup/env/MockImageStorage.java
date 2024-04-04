package ecsimsw.picup.env;

import ecsimsw.picup.domain.StoredFile;
import ecsimsw.picup.storage.StorageKey;
import ecsimsw.picup.service.ImageStorage;
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
    public CompletableFuture<String> storeAsync(String resourceKey, StoredFile storedFile) {
        if (storedFile == null) {
            return new AsyncResult<>(resourceKey).completable();
        }
        DATA.put(resourceKey, storedFile.file());
        return new AsyncResult<>(resourceKey).completable();
    }

    @Override
    public StoredFile read(String resourceKey) {
        return StoredFile.of(resourceKey, DATA.get(resourceKey));
    }

    @Override
    public void deleteIfExists(String resourceKey) {
        DATA.remove(resourceKey);
    }
}
