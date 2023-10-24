package ecsimsw.picup.storage;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.StorageKey;

public interface ImageStorage {

    StorageKey key();

    void create(String resourceKey, ImageFile imageFile);

    ImageFile read(String resourceKey);

    void delete(String resourceKey);
}
