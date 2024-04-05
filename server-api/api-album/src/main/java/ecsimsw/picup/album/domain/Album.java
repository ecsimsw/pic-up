package ecsimsw.picup.album.domain;

import ecsimsw.picup.auth.UnauthorizedException;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
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

    @NotNull
    private Long userId;

    @NotBlank
    private String name;

    @NotBlank
    private String resourceKey;

    @Min(0)
    private long resourceFileSize;

    @NotNull
    private LocalDateTime createdAt;

    public Album(Long userId, String name, String resourceKey, long resourceFileSize) {
        this(null, userId, name, resourceKey, resourceFileSize, LocalDateTime.now());
    }

    public void authorize(Long userId) {
        if (!this.userId.equals(userId)) {
            throw new UnauthorizedException("User doesn't have permission on this album");
        }
    }
}
