package ecsimsw.picup.dto;

import ecsimsw.picup.auth.LoginUser;
import ecsimsw.picup.domain.Member;

public record MemberResponse(
    Long id,
    String username
//    long limitAsByte,
//    long usageAsByte
) {

    public static MemberResponse of(Member member) {
        return new MemberResponse(
            member.getId(),
            member.getUsername()
        );
    }

    public LoginUser toTokenPayload() {
        return new LoginUser(id, username);
    }
}
