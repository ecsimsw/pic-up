package ecsimsw.picup.album.dto;

import ecsimsw.picup.auth.AuthTokenPayload;
import ecsimsw.picup.album.domain.Member;
import ecsimsw.picup.album.domain.StorageUsage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class MemberInfoResponse {

    private Long id;
    private String username;
    private long limitAsByte;
    private long usageAsByte;

    public static MemberInfoResponse of(Member member, StorageUsage usage) {
        return new MemberInfoResponse(
            member.getId(),
            member.getUsername(),
            usage.getLimitAsByte(),
            usage.getUsageAsByte()
        );
    }

    public AuthTokenPayload toTokenPayload() {
        return new AuthTokenPayload(id, username);
    }
}