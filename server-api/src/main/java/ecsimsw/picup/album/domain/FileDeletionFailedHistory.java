package ecsimsw.picup.album.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class FileDeletionFailedHistory {

    @Id
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private StorageType storageType;

    @Embedded
    private ResourceKey resourceKey;

    public FileDeletionFailedHistory(StorageType storageType, ResourceKey resourceKey) {
        this(LocalDateTime.now(), storageType, resourceKey);
    }

    public static FileDeletionFailedHistory from(StorageResource resource) {
        return new FileDeletionFailedHistory(
            resource.getStorageType(),
            resource.getResourceKey()
        );
    }
}
