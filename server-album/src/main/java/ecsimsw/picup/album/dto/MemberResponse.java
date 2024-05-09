package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Member;
import ecsimsw.picup.album.domain.StorageUsage;
import ecsimsw.picup.auth.LoginUser;

public record MemberResponse(
    Long id,
    String username,
    long limitAsByte,
    long usageAsByte
) {

    public static MemberResponse of(Member member, StorageUsage usage) {
        return new MemberResponse(
            member.getId(),
            member.getUsername(),
            usage.getLimitAsByte(),
            usage.getUsageAsByte()
        );
    }

    public LoginUser toTokenPayload() {
        return new LoginUser(id, username);
    }
}
