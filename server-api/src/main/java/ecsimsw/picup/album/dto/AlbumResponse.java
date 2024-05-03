package ecsimsw.picup.album.dto;

import static ecsimsw.picup.album.service.FileResourceService.ROOT_PATH;

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

    public static AlbumResponse of(Album album) {
        return new AlbumResponse(
            album.getId(),
            album.getName(),
            ROOT_PATH + album.getThumbnail().value(),
            album.getCreatedAt()
        );
    }

    public static List<AlbumResponse> listOf(List<Album> albums) {
        return albums.stream()
            .map(AlbumResponse::of)
            .collect(Collectors.toList());
    }

    public AlbumResponse sign(String thumbnailSignedUrl) {
        return new AlbumResponse(
            id,
            name,
            thumbnailSignedUrl,
            createdAt
        );
    }
}
