package ecsimsw.picup.album.dto;

import static ecsimsw.picup.config.S3Config.ROOT_PATH;

import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.PictureFileExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record PictureInfoResponse(
    Long id,
    Long albumId,
    boolean isVideo,
    String resourceUrl,
    String thumbnailUrl,
    LocalDateTime createdAt
) {

    public static PictureInfoResponse of(Picture picture) {
        return new PictureInfoResponse(
            picture.getId(),
            picture.getAlbum().getId(),
            PictureFileExtension.fromFileName(picture.getResourceKey()).isVideo,
            ROOT_PATH + picture.getResourceKey(),
            ROOT_PATH + picture.getThumbnailResourceKey(),
            picture.getCreatedAt()
        );
    }

    public static List<PictureInfoResponse> listOf(List<Picture> pictures) {
        return pictures.stream()
            .map(PictureInfoResponse::of)
            .collect(Collectors.toList());
    }
}
