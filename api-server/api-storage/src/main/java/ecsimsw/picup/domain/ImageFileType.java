package ecsimsw.picup.domain;

import ecsimsw.picup.exception.InvalidResourceException;
import org.springframework.http.MediaType;

import java.util.Arrays;

public enum ImageFileType {
    JPEG(MediaType.IMAGE_JPEG),
    JPG(MediaType.IMAGE_JPEG),
    PNG(MediaType.IMAGE_PNG),
    HEIC(MediaType.IMAGE_PNG);

    private final MediaType mediaType;

    ImageFileType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public static ImageFileType extensionOf(String fileName) {
        final String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return Arrays.stream(values())
            .filter(it -> it.name().equals(extension.toUpperCase()))
            .findAny()
            .orElseThrow(() -> new InvalidResourceException("Invalid resource image type"));
    }

    public MediaType getMediaType() {
        return mediaType;
    }
}
