package ecsimsw.picup.domain;

import ecsimsw.picup.exception.InvalidResourceException;
import lombok.Getter;
import org.springframework.http.MediaType;

import java.util.Arrays;

@Getter
public enum ImageFileType {
    JPEG(MediaType.IMAGE_JPEG),
    JPG(MediaType.IMAGE_JPEG),
    PNG(MediaType.IMAGE_PNG);

    private final MediaType mediaType;

    ImageFileType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public static ImageFileType extensionOf(String fileName) {
        var extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return Arrays.stream(values())
            .filter(it -> it.isType(extension.toUpperCase()))
            .findAny()
            .orElseThrow(() -> new InvalidResourceException("Invalid resource image type"));
    }

    public boolean isType(String extension) {
        return this.name().equalsIgnoreCase(extension);
    }
}
