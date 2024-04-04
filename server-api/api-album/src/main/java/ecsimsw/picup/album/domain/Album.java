package ecsimsw.picup.album.domain;

import ecsimsw.picup.auth.UnauthorizedException;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Table(indexes = {
//    @Index(name = "idx_post_title_and_content", columnList = "post_title, post_content")
})
@Entity
public class Album {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull
    private Long userId;

    @NotBlank
    private String name;

    @NotBlank
    private String resourceKey;

    @Min(0)
    private long resourceFileSize;

    @NotNull
    private final LocalDateTime createdAt = LocalDateTime.now();

    public Album(Long id, Long userId, String name, String resourceKey, long resourceFileSize) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.resourceKey = resourceKey;
        this.resourceFileSize = resourceFileSize;
    }

    public Album(Long userId, String name, String resourceKey, Long thumbnailFileSize) {
        this(null, userId, name, resourceKey, thumbnailFileSize);
    }

    public void authorize(Long userId) {
        if (!this.userId.equals(userId)) {
            throw new UnauthorizedException("User doesn't have permission on this album");
        }
    }
}
