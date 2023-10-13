package ecsimsw.picup.dto;

import ecsimsw.picup.domain.Album;
import lombok.Getter;

@Getter
public class UpdateAlbumOrderRequest {

    private final Long albumId;
    private final int order;

    public UpdateAlbumOrderRequest(Long albumId, int order) {
        this.albumId = albumId;
        this.order = order;
    }

    public boolean isAlbum(Album album) {
        return album.getId().equals(this.albumId);
    }
}
