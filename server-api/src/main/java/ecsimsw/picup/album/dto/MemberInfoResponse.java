package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Member;
import ecsimsw.picup.album.domain.StorageUsage;
import ecsimsw.picup.auth.AuthTokenPayload;

public record MemberInfoResponse(
    Long id,
    String username,
    long limitAsByte,
    long usageAsByte
) {

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
