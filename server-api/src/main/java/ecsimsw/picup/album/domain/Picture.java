package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
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

    @JoinColumn(name="albumId", nullable=false)
    @NotNull
    @ManyToOne
    private Album album;

    @Column(nullable = false)
    @NotBlank
    private String resourceKey;

    @Column(nullable = false)
    @NotBlank
    private String thumbnailResourceKey;

    @Column(nullable = false)
    @Min(0)
    private long fileSize;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Picture(Long id, Album album, String resourceKey, String thumbnailResourceKey, long fileSize, LocalDateTime createdAt) {
        if(album == null || resourceKey.isBlank() || thumbnailResourceKey.isBlank() || fileSize < 0) {
            throw new AlbumException("Invalid picture format");
        }
        this.id = id;
        this.album = album;
        this.resourceKey = resourceKey;
        this.thumbnailResourceKey = thumbnailResourceKey;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
    }

    public Picture(Album album, String resourceKey, String thumbnailResourceKey, long fileSize) {
        this(null, album, resourceKey, thumbnailResourceKey, fileSize, LocalDateTime.now());
    }

    public void checkSameUser(Long userId) {
        album.authorize(userId);
    }
}
