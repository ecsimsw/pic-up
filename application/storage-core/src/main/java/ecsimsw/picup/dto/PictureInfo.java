package ecsimsw.picup.dto;

import ecsimsw.picup.domain.Picture;
import ecsimsw.picup.domain.ResourceKey;

import java.time.LocalDateTime;
import java.util.List;

public record PictureInfo(
    Long id,
    Long albumId,
    boolean isVideo,
    boolean hasThumbnail,
    ResourceKey resourceKey,
    LocalDateTime createdAt
) {
    public static PictureInfo of(Picture picture) {
        return new PictureInfo(
            picture.getId(),
            picture.getAlbum().getId(),
            picture.extension().isVideo,
            picture.getHasThumbnail(),
            picture.getFileResource(),
            picture.getCreatedAt()
        );
    }

    public static List<PictureInfo> listOf(List<Picture> pictures) {
        return pictures.stream()
            .map(PictureInfo::of)
            .toList();
    }
}
