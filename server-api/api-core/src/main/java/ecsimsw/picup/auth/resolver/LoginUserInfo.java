package ecsimsw.picup.auth.resolver;

import ecsimsw.picup.auth.dto.AuthTokenPayload;
import ecsimsw.picup.auth.exception.UnauthorizedException;
import lombok.Getter;

@Getter
public class LoginUserInfo {

    private final Long id;
    private final String username;

    public LoginUserInfo(Long id, String username) {
        if(id == null || username == null) {
            throw new UnauthorizedException("Invalid user");
        }
        this.id = id;
        this.username = username;
    }

    public static LoginUserInfo of(AuthTokenPayload payload) {
        return new LoginUserInfo(payload.getId(), payload.getUsername());
    }
}
