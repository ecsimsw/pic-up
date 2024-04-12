package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Album;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public record AlbumInfoResponse(
    Long id,
    String name,
    String thumbnailImage,
    LocalDateTime createdAt
) {

    public static AlbumInfoResponse of(Album album) {
        return new AlbumInfoResponse(
            album.getId(),
            album.getName(),
            album.getResourceKey(),
            album.getCreatedAt()
        );
    }

    public static List<AlbumInfoResponse> listOf(List<Album> albums) {
        return albums.stream()
            .map(AlbumInfoResponse::of)
            .collect(Collectors.toList());
    }
}