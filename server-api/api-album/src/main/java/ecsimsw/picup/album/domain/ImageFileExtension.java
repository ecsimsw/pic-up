package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.exception.UnsupportedFileTypeException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Objects;

public enum ImageFileExtension {
    JPEG, JPG, PNG;

    public static ImageFileExtension of(String value) {
        return Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(value))
            .findAny()
            .orElseThrow(() -> new UnsupportedFileTypeException("Invalid file type"));
    }

    public static ImageFileExtension fromFileName(String fileName) {
        return of(fileName.substring(fileName.lastIndexOf(".") + 1));
    }

    public static void validate(MultipartFile file) {
        var fileName = file.getOriginalFilename();
        if (Objects.isNull(fileName) || !fileName.contains(".")) {
            throw new AlbumException("Invalid file name");
        }
        var extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        of(extension);
    }
}
