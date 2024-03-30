package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Picture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public record PictureSearchCursor(
    Long id,
    LocalDateTime createdAt
) {
    public PictureSearchCursor(Picture picture) {
        this(picture.getId(), picture.getCreatedAt());
    }
}
