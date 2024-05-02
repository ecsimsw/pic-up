package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@Embeddable
public class ResourceKey {

    @Column(nullable = false, length = 50)
    private String resourceKey;

    public ResourceKey(String resourceKey) {
        if (resourceKey.isBlank()) {
            throw new AlbumException("Invalid resource key");
        }
        this.resourceKey = resourceKey;
    }

    public static ResourceKey fromFileName(String fileName) {
        if(fileName == null || !fileName.contains(".")) {
            throw new AlbumException("Invalid file name");
        }
        int indexOfExtension = fileName.lastIndexOf(".");
        var extension = fileName.substring(indexOfExtension + 1);
        return fromExtension(extension);
    }

    public static ResourceKey fromMultipartFile(MultipartFile file) {
        var fileName = file.getOriginalFilename();
        return fromFileName(fileName);
    }

    public static ResourceKey fromExtension(String extension) {
        return new ResourceKey(UUID.randomUUID() + "." + extension);
    }

    public String extension() {
        int indexOfExtension = resourceKey.lastIndexOf(".");
        return resourceKey.substring(indexOfExtension + 1);
    }

    public String value() {
        return resourceKey;
    }
}


