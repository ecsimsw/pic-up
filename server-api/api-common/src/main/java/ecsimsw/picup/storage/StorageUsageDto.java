package ecsimsw.picup.storage;

import lombok.Getter;

@Getter
public class StorageUsageDto {

    private Long userId;
    private long limitAsByte;
    private long usageAsByte;

    public StorageUsageDto() {
    }

    public StorageUsageDto(Long userId, long limitAsByte) {
        this(userId, limitAsByte, 0L);
    }

    public StorageUsageDto(Long userId, long limitAsByte, long usageAsByte) {
        this.userId = userId;
        this.limitAsByte = limitAsByte;
        this.usageAsByte = usageAsByte;
    }
}
