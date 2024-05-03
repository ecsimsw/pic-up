package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Picture;

import java.time.LocalDateTime;

public record PictureResponse(
    Long id,
    Long albumId,
    boolean isVideo,
    String resourceUrl,
    String thumbnailUrl,
    LocalDateTime createdAt
) {
    public static PictureResponse of(Picture picture, String resourceUrl, String thumbnailUrl) {
        return new PictureResponse(
            picture.getId(),
            picture.getAlbum().getId(),
            picture.extension().isVideo,
            resourceUrl,
            thumbnailUrl,
            picture.getCreatedAt()
        );
    }
}
