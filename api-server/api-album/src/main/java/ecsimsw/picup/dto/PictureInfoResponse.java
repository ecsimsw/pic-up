package ecsimsw.picup.dto;

import ecsimsw.picup.domain.Picture;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PictureInfoResponse {

    private final Long id;
    private final Long albumId;
    private final String description;
    private final String resourceKey;
    private final int order;

    public PictureInfoResponse(Long id, Long albumId, String description, String resourceKey, int order) {
        this.id = id;
        this.albumId = albumId;
        this.description = description;
        this.resourceKey = resourceKey;
        this.order = order;
    }

    public static PictureInfoResponse of(Picture picture) {
        return new PictureInfoResponse(
            picture.getId(),
            picture.getAlbumId(),
            picture.getDescription(),
            picture.getResourceKey(),
            picture.getOrderNumber()
        );
    }

    public static List<PictureInfoResponse> listOf(List<Picture> pictures) {
        return pictures.stream()
            .map(PictureInfoResponse::of)
            .collect(Collectors.toList());
    }
}
