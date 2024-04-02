package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.exception.UnsupportedFileTypeException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Objects;

public enum FileExtension {
    JPEG, JPG, PNG;

    public static FileExtension of(String value) {
        return Arrays.stream(values())
            .filter(it -> it.name().equals(value.toUpperCase()))
            .findAny()
            .orElseThrow(() -> new UnsupportedFileTypeException("Invalid file type"));
    }

    public static void validate(MultipartFile file) {
        var fileName = file.getOriginalFilename();
        System.out.println("name : "  + fileName);
        if (Objects.isNull(fileName) || !fileName.contains(".")) {
            throw new AlbumException("Invalid file name");
        }
        var extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        of(extension);
    }
}
