package ecsimsw.picup.dto;

import ecsimsw.picup.domain.Member;

public record MemberResponse(
    Long id,
    String username
) {

    public static MemberResponse of(Member member) {
        return new MemberResponse(
            member.getId(),
            member.getUsername()
        );
    }
}
