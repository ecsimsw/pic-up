package ecsimsw.picup.dto;

import ecsimsw.picup.storage.ImageFile;
import lombok.Getter;

@Getter
public class StorageResourceResponse {

    private final long size;
    private final byte[] file;

    public StorageResourceResponse(long size, byte[] file) {
        this.size = size;
        this.file = file;
    }

    public static StorageResourceResponse of(ImageFile imageFile) {
        return new StorageResourceResponse(
            imageFile.getSize(),
            imageFile.getFile()
        );
    }
}
