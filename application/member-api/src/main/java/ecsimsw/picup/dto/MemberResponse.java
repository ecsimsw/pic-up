package ecsimsw.picup.dto;

public record MemberResponse(
    Long id,
    String username,
    long limitAsByte,
    long usageAsByte
) {

    public static MemberResponse of(MemberInfo member, StorageUsageResponse usage) {
        return new MemberResponse(
            member.id(),
            member.username(),
            usage.limitAsByte(),
            usage.usageAsByte()
        );
    }

    public static MemberResponse of(MemberInfo member) {
        return new MemberResponse(
            member.id(),
            member.username(),
            0,
            0
        );
    }
}
