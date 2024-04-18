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

    @AttributeOverride(name = "resourceKey", column = @Column(name = "resourceKey"))
    @Embedded
    private ResourceKey resourceKey;

    @AttributeOverride(name = "resourceKey", column = @Column(name = "thumbnailResourceKey"))
    @Embedded
    private ResourceKey thumbnailResourceKey;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Picture(Long id, Album album, ResourceKey resourceKey, ResourceKey thumbnailResourceKey, long fileSize, LocalDateTime createdAt) {
        if (album == null || resourceKey == null || thumbnailResourceKey == null || fileSize < 0) {
            throw new AlbumException("Invalid picture format");
        }
        this.id = id;
        this.album = album;
        this.resourceKey = resourceKey;
        this.thumbnailResourceKey = thumbnailResourceKey;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
    }

    public Picture(Album album, ResourceKey resourceKey, ResourceKey thumbnailResourceKey, long fileSize) {
        this(null, album, resourceKey, thumbnailResourceKey, fileSize, LocalDateTime.now());
    }

    public void checkSameUser(Long userId) {
        album.authorize(userId);
    }

    public PictureFileExtension extension() {
        return PictureFileExtension.of(resourceKey.extension());
    }
}
