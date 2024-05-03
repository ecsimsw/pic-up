package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Picture;

import java.time.LocalDateTime;

public record PictureResponse(
    Long id,
    Long albumId,
    boolean isVideo,
    boolean hasThumbnail,
    String resourceUrl,
    String thumbnailUrl,
    LocalDateTime createdAt
) {
    public static PictureResponse of(Picture picture, String resourceUrl, String thumbnailUrl) {
        return new PictureResponse(
            picture.getId(),
            picture.getAlbum().getId(),
            picture.extension().isVideo,
            true,
            resourceUrl,
            thumbnailUrl,
            picture.getCreatedAt()
        );
    }

    public static PictureResponse of(Picture picture, String resourceUrl) {
        return new PictureResponse(
            picture.getId(),
            picture.getAlbum().getId(),
            picture.extension().isVideo,
            false,
            resourceUrl,
            resourceUrl,
            picture.getCreatedAt()
        );
    }
}
