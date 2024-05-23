package ecsimsw.picup.dto;

import ecsimsw.picup.domain.Member;

public record MemberInfo(
    Long id,
    String username
) {
    public static MemberInfo of(Member member) {
        return new MemberInfo(
            member.getId(),
            member.getUsername()
        );
    }
}
