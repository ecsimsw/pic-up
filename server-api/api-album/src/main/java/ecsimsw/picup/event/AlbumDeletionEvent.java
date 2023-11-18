package ecsimsw.picup.event;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class AlbumDeletionEvent {

    private final LocalDateTime creationTime = LocalDateTime.now();
    private final Long albumId;

    public AlbumDeletionEvent(Long albumId) {
        this.albumId = albumId;
    }
}
