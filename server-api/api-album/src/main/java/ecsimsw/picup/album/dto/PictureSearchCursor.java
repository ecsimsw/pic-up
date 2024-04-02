package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Picture;

import java.time.LocalDateTime;
import java.util.Optional;

public record PictureSearchCursor(
    boolean hasPrev,
    int limit,
    Long cursorId,
    LocalDateTime createdAt
) {

    public PictureSearchCursor(Picture picture) {
        this(true, 10, picture.getId(), picture.getCreatedAt());
    }

    public static PictureSearchCursor from(int limit, Optional<Long> cursorId, Optional<LocalDateTime> cursorCreatedAt) {
        if (cursorId.isEmpty() || cursorCreatedAt.isEmpty()) {
            return new PictureSearchCursor(false, limit, null, null);
        }
        return new PictureSearchCursor(true, limit, cursorId.get(), cursorCreatedAt.get());
    }
}
