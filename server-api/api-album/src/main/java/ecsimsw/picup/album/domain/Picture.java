package ecsimsw.picup.album.domain;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
public class Picture {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne
    private Album album;

    @NotBlank
    private String resourceKey;

    @NotBlank
    private String thumbnailResourceKey;

    @Min(0)
    private long fileSize;

    @NotNull
    private final LocalDateTime createdAt = LocalDateTime.now();

    public Picture(Long id, Album album, String resourceKey, String thumbnailResourceKey, long fileSize) {
        this.id = id;
        this.album = album;
        this.resourceKey = resourceKey;
        this.thumbnailResourceKey = thumbnailResourceKey;
        this.fileSize = fileSize;
    }

    public Picture(Album album, String resourceKey, String thumbnailResourceKey, Long fileSize) {
        this(null, album, resourceKey, thumbnailResourceKey, fileSize);
    }

    public void checkSameUser(Long userId) {
        album.authorize(userId);
    }
}
