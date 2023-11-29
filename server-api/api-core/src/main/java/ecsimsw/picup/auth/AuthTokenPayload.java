package ecsimsw.picup.auth;

import ecsimsw.auth.anotations.TokenKey;
import lombok.Getter;
import lombok.Setter;

// XXX :: Do not remove default constructor
//  This dto is deserialized with jackson, and it needs default constructor.

@Setter
@Getter
public class AuthTokenPayload {

    private Long id;

    @TokenKey
    private String username;

    public AuthTokenPayload() {
    }

    public AuthTokenPayload(Long id, String username) {
        this.id = id;
        this.username = username;
    }
}
