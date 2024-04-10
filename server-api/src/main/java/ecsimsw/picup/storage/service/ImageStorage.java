package ecsimsw.picup.storage.service;


import ecsimsw.picup.album.dto.FileUploadResponse;

import java.util.concurrent.CompletableFuture;

public interface ImageStorage {

    CompletableFuture<String> storeAsync(String resourceKey, FileUploadResponse fileUploadResponse);

    FileUploadResponse read(String resourceKey);

    void deleteIfExists(String resourceKey);
}
