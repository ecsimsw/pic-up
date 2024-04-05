package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Picture;

import java.time.LocalDateTime;
import java.util.Optional;

public record PictureSearchCursor(
    int limit,
    LocalDateTime createdAt
) {

    public static PictureSearchCursor from(int limit, Optional<LocalDateTime> cursorCreatedAt) {
        return new PictureSearchCursor(limit, cursorCreatedAt.orElse(LocalDateTime.now()));
    }
}
