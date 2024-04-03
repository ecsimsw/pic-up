package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Album;

import java.time.LocalDateTime;
import java.util.Optional;

public record AlbumSearchCursor(
    boolean hasPrev,
    int limit,
    Long cursorId,
    LocalDateTime createdAt
) {

    public static AlbumSearchCursor from(int limit, Optional<Long> cursorId, Optional<LocalDateTime> cursorCreatedAt) {
        if (cursorId.isEmpty() || cursorCreatedAt.isEmpty()) {
            return new AlbumSearchCursor(false, limit, null, null);
        }
        return new AlbumSearchCursor(true, limit, cursorId.get(), cursorCreatedAt.get());
    }

    public AlbumSearchCursor(int limit, Album album) {
        this(true, limit, album.getId(), album.getCreatedAt());
    }
}
