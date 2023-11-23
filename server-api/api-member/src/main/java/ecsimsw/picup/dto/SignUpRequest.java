package ecsimsw.picup.dto;

import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
public class SignUpRequest {

    @NotBlank
    private final String username;

    @NotBlank
    private final String password;

    public SignUpRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
