package ecsimsw.picup.dto;

import java.time.LocalDateTime;

public record AlbumResponse(
    Long id,
    String name,
    String thumbnailUrl,
    LocalDateTime createdAt
) {

    public static AlbumResponse of(AlbumInfo albumInfo, String thumbnailUrl) {
        return new AlbumResponse(
            albumInfo.id(),
            albumInfo.name(),
            thumbnailUrl,
            albumInfo.createdAt()
        );
    }
}
