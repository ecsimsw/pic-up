package ecsimsw.picup.utils;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.StorageKey;
import ecsimsw.picup.storage.ImageStorage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockImageStorage implements ImageStorage {

    private static final Map<String, byte[]> DATA = new ConcurrentHashMap<>();

    private final StorageKey key;

    public MockImageStorage(StorageKey key) {
        this.key = key;
    }

    @Override
    public void create(String resourceKey, ImageFile imageFile) {
        DATA.put(resourceKey, imageFile.getFile());
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
        return key;
    }
}
