package ecsimsw.picup.album.dto;

import javax.validation.constraints.NotBlank;

public record SignUpRequest(
    @NotBlank String username,
    @NotBlank String password
) {

}
