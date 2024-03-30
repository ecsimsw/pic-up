package ecsimsw.picup.album.dto;

import javax.validation.constraints.NotBlank;

public record PictureInfoRequest(
    @NotBlank String description
) {
}
