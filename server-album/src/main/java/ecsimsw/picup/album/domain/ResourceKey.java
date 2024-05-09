package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.core.appender.rolling.FileExtension;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.Embeddable;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Embeddable
public class ResourceKey {

    @Convert(converter = AesStringConverter.class)
    @Column(nullable = false)
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

    public PictureFileExtension extension() {
        int indexOfExtension = resourceKey.lastIndexOf(".");
        String extension = resourceKey.substring(indexOfExtension + 1);
        return PictureFileExtension.of(extension);
    }

    public String value() {
        return resourceKey;
    }
}


