package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Picture;
import java.time.LocalDateTime;
import java.util.List;

public record PictureResponse(
    Long id,
    Long albumId,
    boolean isVideo,
    String resourceUrl,
    String thumbnailUrl,
    LocalDateTime createdAt
) {

    public static PictureResponse of(Picture picture) {
        return new PictureResponse(
            picture.getId(),
            picture.getAlbum().getId(),
            picture.extension().isVideo,
            picture.getResourceKey().value(),
            picture.getThumbnailResourceKey().value(),
            picture.getCreatedAt()
        );
    }

    public static List<PictureResponse> listOf(List<Picture> pictures) {
        return pictures.stream()
            .map(PictureResponse::of)
            .toList();
    }
}
