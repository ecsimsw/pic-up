package ecsimsw.picup.domain;

import ecsimsw.picup.exception.AlbumException;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Table(indexes = {
    @Index(name = "idx_userId_createdAt", columnList = "id, createdAt")
})
@Entity
public class Album {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false)
    private Long userId;

//    @Convert(converter = AesStringConverter.class)
    @Column
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

    public Album(Long id, Long userId, String name, ResourceKey thumbnail) {
        this(id, userId, name, thumbnail, LocalDateTime.now());
    }

    public Album(Long userId, String name, ResourceKey thumbnail) {
        this(null, userId, name, thumbnail, LocalDateTime.now());
    }

    public void authorize(Long userId) {
        if (!this.userId.equals(userId)) {
            throw new AlbumException("User doesn't have permission on this album");
        }
    }
}
