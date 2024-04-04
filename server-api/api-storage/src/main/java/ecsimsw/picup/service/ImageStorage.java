package ecsimsw.picup.service;

import ecsimsw.picup.domain.StoredFile;
import java.util.concurrent.CompletableFuture;

public interface ImageStorage {

    CompletableFuture<String> storeAsync(String resourceKey, StoredFile storedFile);

    StoredFile read(String resourceKey);

    void deleteIfExists(String resourceKey);
}
