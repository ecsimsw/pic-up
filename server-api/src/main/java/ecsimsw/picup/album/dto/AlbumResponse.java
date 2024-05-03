package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Album;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record AlbumResponse(
    Long id,
    String name,
    String thumbnailUrl,
    LocalDateTime createdAt
) {

    public static AlbumResponse of(Album album, String thumbnailUrl) {
        return new AlbumResponse(
            album.getId(),
            album.getName(),
            thumbnailUrl,
            album.getCreatedAt()
        );
    }
}
