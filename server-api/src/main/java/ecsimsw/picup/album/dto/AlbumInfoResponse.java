package ecsimsw.picup.album.dto;

import static ecsimsw.picup.config.S3Config.ROOT_PATH;

import ecsimsw.picup.album.domain.Album;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record AlbumInfoResponse(
    Long id,
    String name,
    String thumbnailUrl,
    LocalDateTime createdAt
) {

    public static AlbumInfoResponse of(Album album) {
        return new AlbumInfoResponse(
            album.getId(),
            album.getName(),
            ROOT_PATH + album.getResourceKey(),
            album.getCreatedAt()
        );
    }

    public static List<AlbumInfoResponse> listOf(List<Album> albums) {
        return albums.stream()
            .map(AlbumInfoResponse::of)
            .collect(Collectors.toList());
    }
}
