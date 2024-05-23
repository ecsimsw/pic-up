package ecsimsw.picup.domain;

import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.utils.FileUtils;

import java.util.Arrays;

public enum FileResourceExtension {
    JPEG(false),
    JPG(false),
    PNG(false),
    MP4(true);

    public final boolean isVideo;

    FileResourceExtension(boolean isVideo) {
        this.isVideo = isVideo;
    }

    public static FileResourceExtension of(String extension) {
        return Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(extension))
            .findAny()
            .orElseThrow(() -> new StorageException("Invalid file type"));
    }
}