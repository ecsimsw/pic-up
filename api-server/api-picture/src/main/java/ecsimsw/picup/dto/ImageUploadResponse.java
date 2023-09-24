package ecsimsw.picup.dto;

import ecsimsw.picup.domain.ImageFile;
import lombok.Getter;

@Getter
public class ImageUploadResponse {

    private final long size;
    private final String name;
    private final long folderId;

    public ImageUploadResponse(long size, String name, long folderId) {
        this.size = size;
        this.name = name;
        this.folderId = folderId;
    }

    public static ImageUploadResponse of(ImageFile imageFile, long folderId) {
        return new ImageUploadResponse(
            imageFile.getSize(),
            imageFile.getName(),
            folderId
        );
    }
}
