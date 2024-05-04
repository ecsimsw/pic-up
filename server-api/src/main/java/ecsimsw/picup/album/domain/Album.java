package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.auth.UnauthorizedException;
import java.time.LocalDateTime;
import javax.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Table(indexes = {
    @Index(name = "idx_userId_createdAt", columnList = "userId, createdAt")
})
@Entity
public class Album {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Convert(converter = AesStringConverter.class)
    @Column(length = 128)
    private String name;

    @Embedded
    private ResourceKey thumbnail;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Album(Long id, Long userId, String name, ResourceKey thumbnail, LocalDateTime createdAt) {
        if (userId == null || name.isBlank()) {
            throw new AlbumException("Invalid album format");
        }
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.thumbnail = thumbnail;
        this.createdAt = createdAt;
    }

    public Album(Long userId, String name, ResourceKey thumbnail) {
        this(null, userId, name, thumbnail, LocalDateTime.now());
    }

    public void authorize(Long userId) {
        if (!this.userId.equals(userId)) {
            throw new UnauthorizedException("User doesn't have permission on this album");
        }
    }
}
