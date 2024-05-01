package ecsimsw.picup.album.dto;

import java.time.LocalDateTime;

public record PictureInfoResponse(
    Long id,
    Long albumId,
    boolean isVideo,
    String resourceUrl,
    String thumbnailUrl,
    LocalDateTime createdAt
) {
}
