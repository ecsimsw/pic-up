package ecsimsw.picup.dto;

import ecsimsw.picup.domain.Member;
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

    public Member toEntity() {
        return new Member(username, password);
    }
}
