package ecsimsw.picup.env;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.StorageKey;
import ecsimsw.picup.dto.StorageUploadResponse;
import ecsimsw.picup.storage.ImageStorage;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class MockImageStorage implements ImageStorage {

    private static final Map<String, byte[]> DATA = new ConcurrentHashMap<>();

    private final StorageKey KEY;

    public MockImageStorage(StorageKey key) {
        this.KEY = key;
    }

    @Override
    public Future<StorageUploadResponse> create(String resourceKey, ImageFile imageFile) {
        DATA.put(resourceKey, imageFile.getFile());
        return new AsyncResult<>(new StorageUploadResponse(resourceKey, KEY, imageFile.getSize()));
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
