package ecsimsw.picup.dto;

import ecsimsw.picup.domain.Album;
import lombok.Getter;

@Getter
public class AlbumInfoResponse {

    private final Long id;
    private final String name;
    private final String thumbnailImage;

    public AlbumInfoResponse(Long id, String name, String thumbnailImage) {
        this.id = id;
        this.name = name;
        this.thumbnailImage = thumbnailImage;
    }

    public static AlbumInfoResponse of(Album album) {
        return new AlbumInfoResponse(album.getId(), album.getName(), album.getThumbnailResourceKey());
    }
}
