package ecsimsw.picup.storage;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.StoragePath;

public interface ImageStorage {

    void create(StoragePath storagePath, ImageFile imageFile);

    ImageFile read(StoragePath path);

    void delete(StoragePath storagePath);
}
