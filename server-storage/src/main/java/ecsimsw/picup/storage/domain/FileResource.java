package ecsimsw.picup.storage.domain;

import java.time.LocalDateTime;
import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {
    @Index(name = "idx_resource_key", columnList = "resourceKey")
})
@Entity
public class FileResource {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private StorageType storageType;

    @Embedded
    private ResourceKey resourceKey;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private Boolean toBeDeleted;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static FileResource toBeDeleted(StorageType storageType, ResourceKey resourceKey, Long fileSize) {
        return new FileResource(storageType, resourceKey, fileSize, true);
    }

    public static FileResource stored(StorageType storageType, ResourceKey resourceKey, Long fileSize) {
        return new FileResource(storageType, resourceKey, fileSize, false);
    }

    public FileResource(StorageType storageType, ResourceKey resourceKey, Long size, Boolean toBeDeleted, LocalDateTime localDateTime) {
        this(null, storageType, resourceKey, size, toBeDeleted, localDateTime);
    }

    public FileResource(StorageType storageType, ResourceKey resourceKey, Long size, Boolean toBeDeleted) {
        this(null, storageType, resourceKey, size, toBeDeleted, LocalDateTime.now());
    }

    public void setToBeDeleted(boolean toBeDeleted) {
        this.toBeDeleted = toBeDeleted;
    }
}
