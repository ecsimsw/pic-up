package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Embeddable
public class ResourceKey {

    @Column(nullable = false)
    private String resourceKey;

    public ResourceKey(String resourceKey) {
        if (resourceKey.isBlank()) {
            throw new AlbumException("Invalid resource key");
        }
        this.resourceKey = resourceKey;
    }

    public static ResourceKey generate(MultipartFile file) {
        var filename = file.getOriginalFilename();
        int indexOfExtension = filename.lastIndexOf(".");
        var extension = filename.substring(indexOfExtension + 1);
        return withExtension(extension);
    }

    public static ResourceKey withExtension(String extension) {
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
