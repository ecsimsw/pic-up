package ecsimsw.picup.utils;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.storage.ImageStorage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MockImageStorage implements ImageStorage {

    private static final Map<String, byte[]> DATA = new ConcurrentHashMap<>();

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
}
