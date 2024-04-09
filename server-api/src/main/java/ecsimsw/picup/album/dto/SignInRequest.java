package ecsimsw.picup.album.dto;

import javax.validation.constraints.NotBlank;

public record SignInRequest(
    @NotBlank String username,
    @NotBlank String password
) {

}
