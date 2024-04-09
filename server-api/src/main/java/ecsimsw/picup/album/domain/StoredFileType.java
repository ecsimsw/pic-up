package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.InvalidResourceException;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum StoredFileType {
    JPEG,
    JPG,
    PNG,
    MP4;

    public static StoredFileType fromFileName(String fileName) {
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
