package ecsimsw.picup.album.dto;

import javax.validation.constraints.NotBlank;

public record AlbumInfoRequest(
    @NotBlank String name
) {
}
