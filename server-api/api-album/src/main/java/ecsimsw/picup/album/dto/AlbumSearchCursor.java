package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Album;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public record AlbumSearchCursor(
    Long id,
    LocalDateTime createdAt
) {

    public AlbumSearchCursor(Album album) {
        this(album.getId(), album.getCreatedAt());
    }
}
