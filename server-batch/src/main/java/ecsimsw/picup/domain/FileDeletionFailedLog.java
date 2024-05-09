package ecsimsw.picup.domain;

import ecsimsw.picup.storage.domain.FileResource;
import ecsimsw.picup.storage.domain.ResourceKey;
import ecsimsw.picup.storage.domain.StorageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class FileDeletionFailedLog {

    @Id
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private StorageType storageType;

    @Embedded
    private ResourceKey resourceKey;

    public FileDeletionFailedLog(StorageType storageType, ResourceKey resourceKey) {
        this(LocalDateTime.now(), storageType, resourceKey);
    }

    public static FileDeletionFailedLog from(FileResource resource) {
        return new FileDeletionFailedLog(
            resource.getStorageType(),
            resource.getResourceKey()
        );
    }
}
