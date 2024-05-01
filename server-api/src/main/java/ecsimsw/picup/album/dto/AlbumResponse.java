package ecsimsw.picup.album.dto;

import static ecsimsw.picup.config.S3Config.ROOT_PATH;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.service.ResourceUrlService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record AlbumResponse(
    Long id,
    String name,
    String thumbnailUrl,
    LocalDateTime createdAt
) {

    public static AlbumResponse of(Album album) {
        return new AlbumResponse(
            album.getId(),
            album.getName(),
            ROOT_PATH + album.getResourceKey().value(),
            album.getCreatedAt()
        );
    }

    public static List<AlbumResponse> listOf(List<Album> albums) {
        return albums.stream()
            .map(AlbumResponse::of)
            .collect(Collectors.toList());
    }
}
