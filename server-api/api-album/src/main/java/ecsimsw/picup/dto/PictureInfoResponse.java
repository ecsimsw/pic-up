package ecsimsw.picup.dto;

import ecsimsw.picup.domain.Picture;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PictureInfoResponse {

    private Long id;
    private Long albumId;
    private String description;
    private String resourceKey;
    private LocalDateTime createdAt;

    public PictureInfoResponse() {
    }

    public PictureInfoResponse(Long id, Long albumId, String description, String resourceKey, LocalDateTime createdAt) {
        this.id = id;
        this.albumId = albumId;
        this.description = description;
        this.resourceKey = resourceKey;
        this.createdAt = createdAt;
    }

    public static PictureInfoResponse of(Picture picture) {
        return new PictureInfoResponse(
            picture.getId(),
            picture.getAlbumId(),
            picture.getDescription(),
            picture.getResourceKey(),
            picture.getCreatedAt()
        );
    }

    public static List<PictureInfoResponse> listOf(List<Picture> pictures) {
        return pictures.stream()
            .map(PictureInfoResponse::of)
            .collect(Collectors.toList());
    }
}
