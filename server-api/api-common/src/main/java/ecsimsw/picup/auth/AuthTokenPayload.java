package ecsimsw.picup.auth;

import ecsimsw.auth.anotations.TokenKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// XXX :: Do not remove default constructor
//  This dto is deserialized with jackson, and it needs default constructor.

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AuthTokenPayload {

    private Long id;

    @TokenKey
    private String username;
}
