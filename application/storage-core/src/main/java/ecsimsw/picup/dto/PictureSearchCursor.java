package ecsimsw.picup.dto;

import java.time.LocalDateTime;
import java.util.Optional;

public record PictureSearchCursor(
    int limit,
    Optional<LocalDateTime> createdAt
) {

    public static PictureSearchCursor from(int limit, Optional<LocalDateTime> createdAt) {
        return new PictureSearchCursor(limit, createdAt);
    }
}
