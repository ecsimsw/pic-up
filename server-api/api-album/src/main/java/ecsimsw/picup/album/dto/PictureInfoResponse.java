package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Picture;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PictureInfoResponse {

    private Long id;
    private Long albumId;
    private String resourceKey;
    private LocalDateTime createdAt;

    public static PictureInfoResponse of(Picture picture) {
        return new PictureInfoResponse(
            picture.getId(),
            picture.getAlbumId(),
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
