package ecsimsw.picup.dto;

import ecsimsw.picup.domain.ImageFile;
import lombok.Getter;

@Getter
public class ImageLoadResponse {

    private final long size;
    private final String name;
    private final byte[] binaryValue;

    public ImageLoadResponse(long size, String name, byte[] binaryValue) {
        this.size = size;
        this.name = name;
        this.binaryValue = binaryValue;
    }

    public static ImageLoadResponse of(ImageFile imageFile) {
        return new ImageLoadResponse(
            imageFile.getSize(),
            imageFile.getName(),
            imageFile.getBinaryValue()
        );
    }
}
