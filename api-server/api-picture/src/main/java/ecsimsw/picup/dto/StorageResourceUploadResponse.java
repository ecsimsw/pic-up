package ecsimsw.picup.dto;

import ecsimsw.picup.storage.ImageFile;
import lombok.Getter;

@Getter
public class StorageResourceUploadResponse {

    private final long size;
    private final String key;

    public StorageResourceUploadResponse(long size, String key) {
        this.size = size;
        this.key = key;
    }

    public static StorageResourceUploadResponse of(ImageFile imageFile, String resourceKey) {
        return new StorageResourceUploadResponse(
            imageFile.getSize(),
            resourceKey
        );
    }
}
