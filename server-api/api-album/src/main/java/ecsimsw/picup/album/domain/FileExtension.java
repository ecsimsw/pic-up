package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.UnsupportedFileTypeException;

import java.util.Arrays;

public enum FileExtension {
    JPEG, JPG, PNG, HEIC;

    public static FileExtension of(String value) {
        return Arrays.stream(values())
            .filter(it -> it.name().equals(value.toUpperCase()))
            .findAny()
            .orElseThrow(() -> new UnsupportedFileTypeException("Invalid file type"));
    }

    public static FileExtension fromFileName(String fileName) {
        final String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return of(extension);
    }
}
