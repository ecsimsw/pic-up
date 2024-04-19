package ecsimsw.picup.env;


import ecsimsw.picup.album.domain.Password;
import ecsimsw.picup.album.dto.SignUpRequest;

public class MemberFixture {

    public static final Long USER_ID = 1L;
    public static final String USER_NAME = "username";
    public static final String USER_PASSWORD = "password";
    public static final Password PASSWORD = new Password(USER_PASSWORD, "SALT");

    public static SignUpRequest SIGN_UP_REQUEST = new SignUpRequest("USERNAME", "PASSWORD");
}
