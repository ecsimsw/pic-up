package ecsimsw.picup.member.dto;

import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class MemberInfoResponse {

    private Long id;
    private String username;

    public static MemberInfoResponse of(Member member) {
        return new MemberInfoResponse(
            member.getId(),
            member.getUsername()
        );
    }

    public AuthTokenPayload toTokenPayload() {
        return new AuthTokenPayload(id, username);
    }
}
