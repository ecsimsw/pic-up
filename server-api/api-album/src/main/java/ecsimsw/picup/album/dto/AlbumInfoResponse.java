package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Album;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlbumInfoResponse {
    private Long id;
    private String name;
    private String thumbnailImage;
    private LocalDateTime createdAt;

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
