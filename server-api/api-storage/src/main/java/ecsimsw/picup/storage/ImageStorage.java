package ecsimsw.picup.storage;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.dto.StorageUploadResponse;

import java.io.FileNotFoundException;
import java.util.concurrent.CompletableFuture;

public interface ImageStorage {

    StorageKey key();

    CompletableFuture<StorageUploadResponse> create(String resourceKey, ImageFile imageFile);

    ImageFile read(String resourceKey) throws FileNotFoundException;

    void delete(String resourceKey) throws FileNotFoundException;
}
