package ecsimsw.picup.dto;

import ecsimsw.picup.domain.Album;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class AlbumInfoResponse {

    private final Long id;
    private final String name;
    private final String thumbnailImage;
    private final int order;

    public AlbumInfoResponse(Long id, String name, String thumbnailImage, int order) {
        this.id = id;
        this.name = name;
        this.thumbnailImage = thumbnailImage;
        this.order = order;
    }

    public static AlbumInfoResponse of(Album album) {
        return new AlbumInfoResponse(
            album.getId(),
            album.getName(),
            album.getResourceKey(),
            album.getOrderNumber()
        );
    }

    public static List<AlbumInfoResponse> listOf(List<Album> albums) {
        return albums.stream()
            .map(AlbumInfoResponse::of)
            .collect(Collectors.toList());
    }
}
