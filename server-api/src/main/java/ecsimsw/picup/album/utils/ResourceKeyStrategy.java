package ecsimsw.picup.album.utils;

import ecsimsw.picup.album.exception.AlbumException;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public class ResourceKeyStrategy {

    public static String generate(MultipartFile file) {
        var fileName = file.getOriginalFilename();
        return generate(fileName);
    }

    public static String generate(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new AlbumException("Invalid file name resource");
        }
        return UUID.randomUUID() + "." + getExtension(fileName);
    }

    private static String getExtension(String fileName) {
        int indexOfExtension = fileName.lastIndexOf(".");
        if (indexOfExtension + 1 >= fileName.length()) {
            throw new AlbumException("Invalid file name resource");
        }
        return fileName.substring(indexOfExtension + 1);
    }
}
