package ecsimsw.picup.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Getter
public class SignUpRequest {

    @NotBlank
    private final String username;

    @NotBlank
    private final String password;
}
