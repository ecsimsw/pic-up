package ecsimsw.picup.dto;

public record StorageUsageResponse(
    long limitAsByte,
    long usageAsByte
) {
}
