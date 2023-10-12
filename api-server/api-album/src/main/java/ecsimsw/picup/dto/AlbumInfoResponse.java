package ecsimsw.picup.dto;

import ecsimsw.picup.domain.Album;
import lombok.Getter;

@Getter
public class AlbumInfoResponse {

    private final String name;
    private final String thumbnailImage;

    public AlbumInfoResponse(String name, String thumbnailImage) {
        this.name = name;
        this.thumbnailImage = thumbnailImage;
    }

    public static AlbumInfoResponse of(Album album) {
        return new AlbumInfoResponse(album.getName(), album.getThumbnailImage());
    }
}
