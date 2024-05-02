package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import java.time.LocalDateTime;
import javax.persistence.*;

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

    @AttributeOverride(name = "resourceKey", column = @Column(name = "fileResource", length = 50))
    @Embedded
    private ResourceKey fileResource;

    @AttributeOverride(name = "resourceKey", column = @Column(name = "thumbnail", length = 50))
    @Embedded
    private ResourceKey thumbnail;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Picture(Long id, Album album, ResourceKey fileResource, ResourceKey thumbnail, long fileSize, LocalDateTime createdAt) {
        if (album == null || fileResource == null || fileSize < 0) {
            throw new AlbumException("Invalid picture format");
        }
        this.id = id;
        this.album = album;
        this.fileResource = fileResource;
        this.thumbnail = thumbnail;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
    }

    public Picture(Album album, ResourceKey fileResource, ResourceKey thumbnail, long fileSize) {
        this(null, album, fileResource, thumbnail, fileSize, LocalDateTime.now());
    }

    public Picture(Album album, String fileResource, long fileSize) {
        this(null, album, new ResourceKey(fileResource), null, fileSize, LocalDateTime.now());
    }

    public void setThumbnail(String resourceKey) {
        if(thumbnail != null) {
            throw new AlbumException("Thumbnail already exists");
        }
        this.thumbnail = new ResourceKey(resourceKey);
    }

    public void checkSameUser(Long userId) {
        this.album.authorize(userId);
    }

    public PictureFileExtension extension() {
        return PictureFileExtension.of(this.fileResource.extension());
    }
}
