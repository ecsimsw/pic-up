package ecsimsw.picup.domain;

import ecsimsw.picup.exception.AlbumException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Setter
@Getter
@Entity
public class StorageUsage {

    @Id
    private Long userId;
    private long limitAsByte;
    private long usageAsByte;

    public StorageUsage() {
    }

    public static StorageUsage initDefaultPlan(Long userId) {
        return new StorageUsage(userId, 10737418240L, 0L);
    }

    public StorageUsage(Long userId, Long limitAsByte, Long usageAsByte) {
        this.userId = userId;
        this.limitAsByte = limitAsByte;
        this.usageAsByte = usageAsByte;
    }

    public boolean isAbleToStore(Long addedSize) {
        return limitAsByte > usageAsByte + addedSize;
    }

    public void add(Long addedSize) {
        if (!isAbleToStore(addedSize)) {
            throw new AlbumException("Not available to store");
        }
        usageAsByte += addedSize;
    }

    public void subtract(long fileSize) {
        if (usageAsByte - fileSize < 0) {
            usageAsByte = 0L;
            return;
        }
        usageAsByte -= fileSize;
    }
}
