package ecsimsw.picup.storage;

import ecsimsw.picup.domain.ImageFile;

import java.io.FileNotFoundException;

public interface ImageStorage {

    void create(String resourceKey, ImageFile imageFile);

    ImageFile read(String resourceKey) throws FileNotFoundException;

    void delete(String resourceKey) throws FileNotFoundException;
}
