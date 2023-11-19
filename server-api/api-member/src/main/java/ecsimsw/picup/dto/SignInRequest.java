package ecsimsw.picup.dto;

import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
public class SignInRequest {

    @NotBlank
    private final String username;

    @NotBlank
    private final String password;

    public SignInRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
