package ecsimsw.picup.storage.domain;

import ecsimsw.picup.storage.exception.StorageException;
import ecsimsw.picup.storage.utils.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Objects;

public enum FileResourceExtension {
    JPEG(false),
    JPG(false),
    PNG(false),
    MP4(true);

    public final boolean isVideo;

    FileResourceExtension(boolean isVideo) {
        this.isVideo = isVideo;
    }

    public static FileResourceExtension fromFileName(String fileName) {
        var extension = FileUtils.getExtensionFromName(fileName);
        return of(extension);
    }

    public static FileResourceExtension of(MultipartFile file) {
        var fileName = Objects.requireNonNull(file.getOriginalFilename());
        var extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return of(extension);
    }

    public static FileResourceExtension of(String extension) {
        return Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(extension))
            .findAny()
            .orElseThrow(() -> new StorageException("Invalid file type"));
    }
}