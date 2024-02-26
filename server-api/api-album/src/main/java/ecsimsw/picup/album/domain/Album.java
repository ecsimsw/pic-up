package ecsimsw.picup.album.domain;

import ecsimsw.picup.auth.UnauthorizedException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Album {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull
    private Long userId;

    private String name;

    private String thumbnailResourceKey;

    private Long thumbnailFileSize;

    private LocalDateTime createdAt;

    public Album(Long userId, String name, String thumbnailResourceKey, Long thumbnailFileSize, LocalDateTime createdAt) {
        this(null, userId, name, thumbnailResourceKey, thumbnailFileSize, createdAt);
    }

    public Album(Long userId, String name, String thumbnailResourceKey, Long thumbnailFileSize) {
        this(null, userId, name, thumbnailResourceKey, thumbnailFileSize, LocalDateTime.now());
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateThumbnail(String thumbnailImage) {
        this.thumbnailResourceKey = thumbnailImage;
    }

    public void authorize(Long userId) {
        if (!this.userId.equals(userId)) {
            throw new UnauthorizedException("User doesn't have permission on this album");
        }
    }
}
