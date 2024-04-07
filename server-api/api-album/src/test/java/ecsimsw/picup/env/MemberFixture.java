package ecsimsw.picup.env;


import ecsimsw.picup.member.dto.SignUpRequest;

public class MemberFixture {

    public static final Long USER_ID = 1L;
    public static SignUpRequest SIGN_UP_REQUEST = new SignUpRequest("USERNAME", "PASSWORD");
}
