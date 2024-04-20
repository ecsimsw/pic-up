package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.exception.UnsupportedFileTypeException;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
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

    public static PictureFileExtension fromFileName(@Nullable String fileName) {
        if(fileName == null || fileName.isBlank()) {
            throw new AlbumException("Invalid file name");
        }
        var indexOfExtension = fileName.lastIndexOf(".");
        var extension = fileName.substring(indexOfExtension + 1);
        return of(extension);
    }

    public static PictureFileExtension of(String extension) {
        return Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(extension))
            .findAny()
            .orElseThrow(() -> new UnsupportedFileTypeException("Invalid file type"));
    }

    public static PictureFileExtension of(MultipartFile file) {
        var fileName = Objects.requireNonNull(file.getOriginalFilename());
        var extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return of(extension);
    }
}