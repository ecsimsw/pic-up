package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.UnsupportedFileTypeException;
import java.util.Arrays;

public enum ImageFileExtension {
    JPEG, JPG, PNG, MP4;

    public static ImageFileExtension of(String value) {
        return Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(value))
            .findAny()
            .orElseThrow(() -> new UnsupportedFileTypeException("Invalid file type"));
    }

    public static ImageFileExtension fromFileName(String fileName) {
        return of(fileName.substring(fileName.lastIndexOf(".") + 1));
    }

}
