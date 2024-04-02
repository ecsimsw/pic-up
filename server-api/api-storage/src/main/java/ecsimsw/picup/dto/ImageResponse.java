package ecsimsw.picup.dto;

import ecsimsw.picup.domain.ImageFile;
import ecsimsw.picup.domain.ImageFileType;
import lombok.Getter;
import org.springframework.http.MediaType;

@Getter
public class ImageResponse {

    private final byte[] imageFile;
    private final ImageFileType fileType;

    public ImageResponse(byte[] imageFile, ImageFileType fileType) {
        this.imageFile = imageFile;
        this.fileType = fileType;
    }

    public static ImageResponse of(ImageFile imageFile) {
        return new ImageResponse(imageFile.file(), imageFile.fileType());
    }

    public MediaType getMediaType() {
        return fileType.getMediaType();
    }
}
