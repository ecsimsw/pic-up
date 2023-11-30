package ecsimsw.picup.dto;

import ecsimsw.picup.storage.StorageKey;
import lombok.Getter;

@Getter
public class StorageUploadResponse {

    private final String resourceKey;
    private final StorageKey storageKey;
    private final long size;

    public StorageUploadResponse(String resourceKey, StorageKey storageKey, long size) {
        this.resourceKey = resourceKey;
        this.storageKey = storageKey;
        this.size = size;
    }
}
