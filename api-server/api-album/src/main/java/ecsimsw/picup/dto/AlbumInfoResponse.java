package ecsimsw.picup.dto;

import ecsimsw.picup.domain.Album;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class AlbumInfoResponse {

    private final Long id;
    private final String name;
    private final String thumbnailImage;
    private final LocalDateTime createdAt;

    public AlbumInfoResponse(Long id, String name, String thumbnailImage, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.thumbnailImage = thumbnailImage;
        this.createdAt = createdAt;
    }

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
