package ecsimsw.picup.album.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class FilePreUploadEvent {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false)
    private Long albumId;

    @Column(nullable = false)
    private Long fileSize;

    @Embedded
    private ResourceKey resourceKey;

    @Column
    private LocalDateTime createdAt;

    public FilePreUploadEvent(Long id, Long albumId, Long fileSize, ResourceKey resourceKey, LocalDateTime createdAt) {
        this.id = id;
        this.albumId = albumId;
        this.fileSize = fileSize;
        this.resourceKey = resourceKey;
        this.createdAt = createdAt;
    }

    public FilePreUploadEvent(Long albumId, Long fileSize, ResourceKey resourceKey, LocalDateTime createdAt) {
        this(null, albumId, fileSize, resourceKey, createdAt);
    }
}
