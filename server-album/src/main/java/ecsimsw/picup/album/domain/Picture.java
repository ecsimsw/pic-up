package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import java.time.LocalDateTime;
import javax.persistence.*;

import ecsimsw.picup.storage.domain.FileResourceExtension;
import ecsimsw.picup.storage.domain.ResourceKey;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Table(indexes = {
    @Index(name = "idx_albumId_createdAt_id", columnList = "albumId, createdAt")
})
@Entity
public class Picture {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @JoinColumn(name = "albumId", nullable = false)
    @ManyToOne
    private Album album;

    @Embedded
    private ResourceKey fileResource;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private Boolean hasThumbnail;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Picture(Long id, Album album, ResourceKey fileResource, boolean hasThumbnail, long fileSize, LocalDateTime createdAt) {
        if (album == null || fileResource == null || fileSize < 0) {
            throw new AlbumException("Invalid picture format");
        }
        this.id = id;
        this.album = album;
        this.fileResource = fileResource;
        this.hasThumbnail = hasThumbnail;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
    }

    public Picture(Album album, ResourceKey fileResource, boolean hasThumbnail, long fileSize) {
        this(null, album, fileResource, hasThumbnail, fileSize, LocalDateTime.now());
    }

    public Picture(Album album, ResourceKey fileResource, long fileSize) {
        this(null, album, fileResource, false, fileSize, LocalDateTime.now());
    }

    public void setHasThumbnail(boolean hasThumbnail) {
        this.hasThumbnail = hasThumbnail;
    }

    public void checkSameUser(Long userId) {
        this.album.authorize(userId);
    }

    public FileResourceExtension extension() {
        return this.fileResource.extension();
    }
}
