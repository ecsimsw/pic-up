package ecsimsw.picup.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthTokenPayload {

    private Long id;
    private String username;

    public AuthTokenPayload() {
    }

    public AuthTokenPayload(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public void checkSameUser(AuthTokenPayload otherAuthToken) {
        if(this.id.equals(otherAuthToken.id) && this.username.equals(otherAuthToken.username)) {
            return;
        }
        throw new IllegalArgumentException("Tokens are not from same user");
    }
}
