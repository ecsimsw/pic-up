package ecsimsw.picup.usage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class StorageUsageDto {

    private Long userId;
    private long limitAsByte;
    private long usageAsByte;

    public StorageUsageDto(Long userId, long limitAsByte) {
        this(userId, limitAsByte, 0L);
    }
}
