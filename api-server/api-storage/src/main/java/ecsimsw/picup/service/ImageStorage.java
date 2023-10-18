package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;

public interface ImageStorage {

    void create(String resourceKey, ImageFile imageFile);

    ImageFile read(String resourceKey);

    void delete(String resourceKey);
}