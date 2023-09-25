package ecsimsw.picup.persistence;

public interface ImageStorage {

    void create(String resourceKey, ImageFile imageFile);

    ImageFile read(String resourceKey);

    void delete(String resourceKey);
}
