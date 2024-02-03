package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Picture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Setter
@Getter
public class PictureSearchCursor {

    private final Long id;
    private final LocalDateTime createdAt;

    public PictureSearchCursor(Picture picture) {
        this(picture.getId(), picture.getCreatedAt());
    }
}
