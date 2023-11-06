package ecsimsw.picup.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PictureSearchCursor {

    private final Long id;
    private final LocalDateTime createdAt;

    public PictureSearchCursor(Long id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }
}
