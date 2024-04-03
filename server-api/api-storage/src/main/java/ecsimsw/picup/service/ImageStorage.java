package ecsimsw.picup.service;

import ecsimsw.picup.domain.ImageFile;
import java.io.FileNotFoundException;
import java.util.concurrent.CompletableFuture;

public interface ImageStorage {

    CompletableFuture<String> storeAsync(String resourceKey, ImageFile imageFile);

    ImageFile read(String resourceKey);

    void deleteIfExists(String resourceKey);
}
