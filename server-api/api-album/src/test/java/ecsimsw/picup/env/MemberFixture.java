package ecsimsw.picup.env;


import ecsimsw.picup.member.domain.Member;

public class MemberFixture {

    public static final Long MEMBER_ID = 1L;

    public static Member MEMBER() {
        return new Member();
    };

}
