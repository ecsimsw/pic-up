package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import org.assertj.core.util.Strings;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public class ResourceKeyStrategy {

    public static String generate(String tag, MultipartFile file) {
        var fileName = file.getOriginalFilename();
        if(fileName == null || fileName.trim().isEmpty()) {
            throw new AlbumException("Invalid file name resource");
        }
        return uniqueResourceName(tag) + "." + getExtension(fileName);
    }

    private static String uniqueResourceName(String tag) {
        return Strings.join(tag,
            UUID.randomUUID().toString()
        ).with("-");
    }

    private static String getExtension(String fileName) {
        int indexOfExtension = fileName.lastIndexOf(".");
        if(indexOfExtension + 1 >= fileName.length()) {
            throw new AlbumException("Invalid file name resource");
        }
        return fileName.substring(indexOfExtension + 1);
    }
}
