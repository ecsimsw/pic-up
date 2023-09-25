package ecsimsw.picup.storage;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.StoragePath;

public interface ImageStorage {

    void create(StoragePath storagePath, ImageFile imageFile);

    void create(String storagePath, ImageFile imageFile);

    ImageFile read(StoragePath storagePath);

    ImageFile read(String storagePath);

    void delete(StoragePath storagePath);

    void delete(String storagePath);
}
