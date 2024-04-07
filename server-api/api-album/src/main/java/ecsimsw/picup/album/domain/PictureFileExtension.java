package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.UnsupportedFileTypeException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Objects;

public enum PictureFileExtension {
    JPEG(false),
    JPG(false),
    PNG(false),
    MP4(true);

    public final boolean isVideo;

    PictureFileExtension(boolean isVideo) {
        this.isVideo = isVideo;
    }

    public static PictureFileExtension of(String value) {
        return Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(value))
            .findAny()
            .orElseThrow(() -> new UnsupportedFileTypeException("Invalid file type"));
    }

    public static PictureFileExtension of(MultipartFile file) {
        var fileName = Objects.requireNonNull(file.getOriginalFilename());
        return fromFileName(fileName);
    }

    public static PictureFileExtension fromFileName(String fileName) {
        return of(fileName.substring(fileName.lastIndexOf(".") + 1));
    }
}