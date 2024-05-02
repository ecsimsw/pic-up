package ecsimsw.picup.album.dto;

import static ecsimsw.picup.config.S3Config.ROOT_PATH;

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
        var fileResourceUrl = ROOT_PATH + picture.getFileResource().value();
        var thumbnailUrl = ROOT_PATH + (picture.getThumbnail() != null ? picture.getThumbnail().value() : fileResourceUrl);
        return new PictureResponse(
            picture.getId(),
            picture.getAlbum().getId(),
            picture.extension().isVideo,
            fileResourceUrl,
            thumbnailUrl,
            picture.getCreatedAt()
        );
    }

    public static List<PictureResponse> listOf(List<Picture> pictures) {
        return pictures.stream()
            .map(PictureResponse::of)
            .toList();
    }
}
