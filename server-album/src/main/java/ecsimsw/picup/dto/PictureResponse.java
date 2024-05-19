package ecsimsw.picup.dto;

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
    public static PictureResponse of(PictureInfo pictureInfo, String resourceUrl, String thumbnailUrl) {
        return new PictureResponse(
            pictureInfo.id(),
            pictureInfo.albumId(),
            pictureInfo.isVideo(),
            true,
            resourceUrl,
            thumbnailUrl,
            pictureInfo.createdAt()
        );
    }

    public static PictureResponse of(PictureInfo pictureInfo, String resourceUrl) {
        return new PictureResponse(
            pictureInfo.id(),
            pictureInfo.albumId(),
            pictureInfo.isVideo(),
            false,
            resourceUrl,
            resourceUrl,
            pictureInfo.createdAt()
        );
    }
}
