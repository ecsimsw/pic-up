package ecsimsw.picup.auth.dto;

import ecsimsw.picup.auth.exception.TokenException;
import lombok.Getter;
import lombok.Setter;

// XXX :: Do not remove default constructor
//  This dto is deserialized with jackson, and it needs default constructor.

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
        throw new TokenException("Tokens are not from same user");
    }
}
