package ecsimsw.picup.storage;

public interface ImageStorage {

    void create(String storagePath, ImageFile imageFile);

    ImageFile read(String storagePath);

    void delete(String storagePath);
}
