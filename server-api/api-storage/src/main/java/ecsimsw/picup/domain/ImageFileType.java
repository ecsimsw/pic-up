package ecsimsw.picup.domain;

import ecsimsw.picup.exception.InvalidResourceException;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum ImageFileType {
    JPEG,
    JPG,
    PNG,
    MP4;

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
