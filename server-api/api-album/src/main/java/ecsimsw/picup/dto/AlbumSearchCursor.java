package ecsimsw.picup.dto;

import ecsimsw.picup.domain.Album;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AlbumSearchCursor {

    private final Long id;
    private final LocalDateTime createdAt;

    public AlbumSearchCursor(Long id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public AlbumSearchCursor(Album album) {
        this(album.getId(), album.getCreatedAt());
    }
}
