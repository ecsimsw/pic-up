package ecsimsw.picup.utils;

import ecsimsw.picup.album.dto.SignUpRequest;

public class MemberFixture {

    public static final Long USER_ID = 1L;
    public static final String USER_NAME = "username";
    public static final String USER_PASSWORD = "password";
    public static final String USER_PASSWORD_SALT = "salt";

    public static SignUpRequest SIGN_UP_REQUEST = new SignUpRequest(USER_NAME, USER_PASSWORD);
}
