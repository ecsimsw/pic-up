package ecsimsw.picup.member.dto;

import javax.validation.constraints.NotBlank;

public record SignInRequest(
    @NotBlank String username,
    @NotBlank String password
) {

}
