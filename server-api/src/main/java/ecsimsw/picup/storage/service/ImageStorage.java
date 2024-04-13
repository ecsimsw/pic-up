package ecsimsw.picup.storage.service;

import ecsimsw.picup.album.dto.FileUploadResponse;

import java.util.concurrent.CompletableFuture;

public interface ImageStorage {

    CompletableFuture<String> storeAsync(String resourceKey, FileUploadResponse fileUploadResponse);

    void deleteIfExists(String resourceKey);
}
