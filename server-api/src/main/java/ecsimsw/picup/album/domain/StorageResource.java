package ecsimsw.picup.album.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class StorageResource {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private StorageType storageType;

    @Embedded
    private ResourceKey resourceKey;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private Boolean toBeDeleted;

    @Column(nullable = false)
    private int deleteFailedCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public StorageResource(StorageType storageType, ResourceKey resourceKey, Long fileSize) {
        this(null, storageType, resourceKey, fileSize, false, 0, LocalDateTime.now());
    }

    public static StorageResource preUpload(StorageType storageType, ResourceKey resourceKey, Long fileSize) {
        return new StorageResource(null, storageType, resourceKey, fileSize, true, 0, LocalDateTime.now());
    }

    public void markToBeDeleted() {
        this.toBeDeleted = true;
    }

    public void countDeleteFailed() {
        this.deleteFailedCount++;
    }
}
