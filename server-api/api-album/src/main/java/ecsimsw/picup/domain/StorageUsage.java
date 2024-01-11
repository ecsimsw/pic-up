package ecsimsw.picup.domain;

import ecsimsw.picup.exception.AlbumException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Setter
@Getter
@Entity
public class StorageUsage {

    @Id
    private Long userId;

    private Long limitAsByte;
    private Long usageAsByte;

    public StorageUsage() {
    }

    public static StorageUsage initDefaultPlan(Long userId) {
//        return new StorageUsage(null, userId, 10000000000L, 0L);
        return new StorageUsage(userId, 100L, 0L);
    }

    public StorageUsage(Long userId, Long limitAsByte, Long usageAsByte) {
        this.userId = userId;
        this.limitAsByte = limitAsByte;
        this.usageAsByte = usageAsByte;
    }

    public boolean isAbleToStore(Long addedSize) {
        return limitAsByte >= usageAsByte + addedSize;
    }

    public void add(Long addedSize) {
        if (!isAbleToStore(addedSize)) {
            throw new AlbumException("Not available to store");
        }
        usageAsByte += addedSize;
    }

    public void subtract(long fileSize) {
        if(usageAsByte - fileSize < 0) {
            usageAsByte = 0L;
            return;
        }
        usageAsByte -= fileSize;
    }
}
