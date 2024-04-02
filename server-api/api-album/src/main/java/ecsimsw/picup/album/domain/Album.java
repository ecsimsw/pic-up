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
    private String resourceKey;
    private Long resourceFileSize;
    private LocalDateTime createdAt;

    public Album(Long userId, String name, String resourceKey, Long thumbnailFileSize) {
        this(null, userId, name, resourceKey, thumbnailFileSize, LocalDateTime.now());
    }

    public void authorize(Long userId) {
        if (!this.userId.equals(userId)) {
            throw new UnauthorizedException("User doesn't have permission on this album");
        }
    }
}
