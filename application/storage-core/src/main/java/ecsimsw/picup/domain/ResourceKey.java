package ecsimsw.picup.domain;

import ecsimsw.picup.exception.StorageException;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Embeddable
public class ResourceKey {

    @Column(nullable = false)
    private String resourceKey;

    public ResourceKey(String resourceKey) {
        if (resourceKey.isBlank()) {
            throw new StorageException("Invalid resource key");
        }
        this.resourceKey = resourceKey;
    }

    public static ResourceKey fromFileName(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new StorageException("Invalid file name : " + fileName);
        }
        int indexOfExtension = fileName.lastIndexOf(".");
        var extension = fileName.substring(indexOfExtension + 1);
        return fromExtension(extension);
    }

    public static ResourceKey fromExtension(String extension) {
        return new ResourceKey(UUID.randomUUID() + "." + extension);
    }

    public FileResourceExtension extension() {
        int indexOfExtension = resourceKey.lastIndexOf(".");
        String extension = resourceKey.substring(indexOfExtension + 1);
        return FileResourceExtension.of(extension);
    }

    public String value() {
        return resourceKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceKey that = (ResourceKey) o;
        return Objects.equals(resourceKey, that.resourceKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceKey);
    }
}


