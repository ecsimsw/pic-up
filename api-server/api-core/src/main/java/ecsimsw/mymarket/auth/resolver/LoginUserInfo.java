package ecsimsw.mymarket.auth.resolver;

import ecsimsw.mymarket.auth.dto.AuthTokenPayload;
import lombok.Getter;

@Getter
public class LoginUserInfo {

    private final Long id;
    private final String username;

    public LoginUserInfo(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public static LoginUserInfo of(AuthTokenPayload payload) {
        return new LoginUserInfo(payload.getId(), payload.getUsername());
    }
}
