package ecsimsw.picup.dto;

import ecsimsw.picup.storage.ImageFile;
import lombok.Getter;

@Getter
public class StorageResourceResponse {

    private final long size;
    private final byte[] binaryValue;

    public StorageResourceResponse(long size, byte[] binaryValue) {
        this.size = size;
        this.binaryValue = binaryValue;
    }

    public static StorageResourceResponse of(ImageFile imageFile) {
        return new StorageResourceResponse(
            imageFile.getSize(),
            imageFile.getBinaryValue()
        );
    }
}
