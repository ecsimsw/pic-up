package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Picture {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull
    private Long albumId;

    private String resourceKey;

    private Long fileSize;

    private LocalDateTime createdAt;

    public Picture(Long albumId, String resourceKey, Long fileSize) {
        this(null, albumId, resourceKey, fileSize, LocalDateTime.now());
    }

    public void validateAlbum(Long albumId) {
        if (!this.albumId.equals(albumId)) {
            throw new AlbumException("Invalid album");
        }
    }
}
