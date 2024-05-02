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
        return new PictureResponse(
            picture.getId(),
            picture.getAlbum().getId(),
            picture.extension().isVideo,
            fileResourceUrl(picture),
            thumbnailUrl(picture),
            picture.getCreatedAt()
        );
    }

    private static String fileResourceUrl(Picture picture) {
        return ROOT_PATH + picture.getFileResource().value();
    }

    private static String thumbnailUrl(Picture picture) {
        if(picture.getThumbnail() == null) {
            return fileResourceUrl(picture);
        }
        return ROOT_PATH + picture.getThumbnail().value();
    }

    public static List<PictureResponse> listOf(List<Picture> pictures) {
        return pictures.stream()
            .map(PictureResponse::of)
            .toList();
    }

    public PictureResponse sign(String resourceSignedUrl, String thumbnailSignedUrl) {
        return new PictureResponse(
            id,
            albumId,
            isVideo,
            resourceSignedUrl,
            thumbnailSignedUrl,
            createdAt
        );
    }
}
