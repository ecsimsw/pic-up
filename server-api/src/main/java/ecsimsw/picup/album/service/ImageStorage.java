package ecsimsw.picup.album.service;


import ecsimsw.picup.album.domain.StoredFile;

import java.util.concurrent.CompletableFuture;

public interface ImageStorage {

    CompletableFuture<String> storeAsync(String resourceKey, StoredFile storedFile);

    StoredFile read(String resourceKey);

    void deleteIfExists(String resourceKey);
}
