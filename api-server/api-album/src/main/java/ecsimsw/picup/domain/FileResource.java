package ecsimsw.picup.domain;

import ecsimsw.picup.dto.ImageFileInfo;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Entity
public class FileResource {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String resourceKey;

    private long size;

    private boolean isGarbage;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    public FileResource() {
    }

    public FileResource(Long id, String resourceKey, long size, boolean isGarbage, LocalDateTime createdAt, LocalDateTime deletedAt) {
        this.id = id;
        this.resourceKey = resourceKey;
        this.size = size;
        this.isGarbage = isGarbage;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public static FileResource created(String resourceKey, long resourceSize) {
        return new FileResource(null, resourceKey, resourceSize, false, LocalDateTime.now(), null);
    }

    public static FileResource created(ImageFileInfo fileInfo) {
        return FileResource.created(fileInfo.getResourceKey(), fileInfo.getSize());
    }

    public void deleted() {
        this.deletedAt = LocalDateTime.now();
    }

    public void markAsGarbage() {
        this.isGarbage = true;
    }
}
