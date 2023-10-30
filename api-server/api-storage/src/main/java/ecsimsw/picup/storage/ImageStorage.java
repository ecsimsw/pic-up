package ecsimsw.picup.storage;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.StorageKey;

import java.io.FileNotFoundException;

public interface ImageStorage {

    StorageKey key();

    void create(String resourceKey, ImageFile imageFile);

    ImageFile read(String resourceKey) throws FileNotFoundException;

    void delete(String resourceKey) throws FileNotFoundException;
}
