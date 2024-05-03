package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.ResourceKey;
import java.time.LocalDateTime;

public record PreUploadResponse(
    ResourceKey resourceKey,
    Long fileSize,
    LocalDateTime createdAt
) {

    public Picture toPicture(Album album) {
        return new Picture(album, resourceKey, fileSize);
    }
}
