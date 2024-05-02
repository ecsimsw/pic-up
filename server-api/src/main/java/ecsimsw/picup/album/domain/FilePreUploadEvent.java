package ecsimsw.picup.album.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class FilePreUploadEvent {

    @Id
    private String resourceKey;

    @Column(nullable = false)
    private Long fileSize;

    @Column
    private LocalDateTime createdAt;

    public FilePreUploadEvent(String resourceKey, Long fileSize, LocalDateTime createdAt) {
        this.resourceKey = resourceKey;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
    }

    public static FilePreUploadEvent init(ResourceKey resourceKey, long fileSize) {
        return new FilePreUploadEvent(resourceKey.value(), fileSize, LocalDateTime.now());
    }
}
