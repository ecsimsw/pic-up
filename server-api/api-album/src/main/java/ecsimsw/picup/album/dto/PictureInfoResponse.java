package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.PictureFileExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record PictureInfoResponse(
    Long id,
    Long albumId,
    boolean isVideo,
    String resourceKey,
    String thumbnailResourceKey,
    LocalDateTime createdAt
) {

    public static PictureInfoResponse of(Picture picture) {
        return new PictureInfoResponse(
            picture.getId(),
            picture.getAlbum().getId(),
            PictureFileExtension.fromFileName(picture.getResourceKey()).isVideo,
            picture.getResourceKey(),
            picture.getThumbnailResourceKey(),
            picture.getCreatedAt()
        );
    }

    public static List<PictureInfoResponse> listOf(List<Picture> pictures) {
        return pictures.stream()
            .map(PictureInfoResponse::of)
            .collect(Collectors.toList());
    }
}
