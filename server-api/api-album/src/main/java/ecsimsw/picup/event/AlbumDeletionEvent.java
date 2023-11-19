package ecsimsw.picup.event;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class AlbumDeletionEvent {

    private final LocalDateTime creationTime = LocalDateTime.now();
    private final Long userId;
    private final Long albumId;

    public AlbumDeletionEvent(Long userId, Long albumId) {
        this.userId = userId;
        this.albumId = albumId;
    }
}
