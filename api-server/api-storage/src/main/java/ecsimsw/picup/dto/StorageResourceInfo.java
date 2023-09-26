package ecsimsw.picup.dto;

import ecsimsw.picup.persistence.ImageFile;
import lombok.Getter;

@Getter
public class StorageResourceInfo {

    private final long size;
    private final String key;
    private final byte[] file;

    public StorageResourceInfo(long size, String key, byte[] file) {
        this.size = size;
        this.key = key;
        this.file = file;
    }

    public static StorageResourceInfo of(ImageFile imageFile, String resourceKey) {
        return new StorageResourceInfo(
            imageFile.getSize(),
            resourceKey,
            imageFile.getFile()
        );
    }
}
