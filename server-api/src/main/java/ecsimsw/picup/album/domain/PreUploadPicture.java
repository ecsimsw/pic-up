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
public class PreUploadPicture {

    @Id
    private String resourceKey;

    @Column(nullable = false)
    private Long fileSize;

    @Column
    private LocalDateTime createdAt;

    public PreUploadPicture(String resourceKey, Long fileSize, LocalDateTime createdAt) {
        this.resourceKey = resourceKey;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
    }

    public static PreUploadPicture init(String fileName, long fileSize) {
        var resourceKey = ResourceKey.fromFileName(fileName);
        return new PreUploadPicture(resourceKey.value(), fileSize, LocalDateTime.now());
    }

    public Picture toPicture(Album album) {
        return new Picture(album, resourceKey, fileSize);
    }
}
