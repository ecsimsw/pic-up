package ecsimsw.picup.dto;

import ecsimsw.picup.domain.Member;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MemberInfoResponse {

    private Long id;
    private String username;

    public MemberInfoResponse(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public static MemberInfoResponse of(Member member) {
        return new MemberInfoResponse(
            member.getId(),
            member.getUsername()
        );
    }
}
