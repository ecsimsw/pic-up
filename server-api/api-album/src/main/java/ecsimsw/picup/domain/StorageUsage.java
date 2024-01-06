package ecsimsw.picup.domain;

import ecsimsw.picup.exception.AlbumException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Setter
@Getter
@Entity
public class StorageUsage {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @NotNull
    private Long userId;

    private Long limitAsByte;
    private Long usageAsByte;

    public StorageUsage() {
    }

    public static StorageUsage initDefaultPlan(Long userId) {
        return new StorageUsage(null, userId, 10737418240L, 0L);
    }

    public StorageUsage(Long id, Long userId, Long limitAsByte, Long usageAsByte) {
        this.id = id;
        this.userId = userId;
        this.limitAsByte = limitAsByte;
        this.usageAsByte = usageAsByte;
    }

    public StorageUsage(Long userId, Long limitAsByte, Long usageAsByte) {
        this(null, userId, limitAsByte, usageAsByte);
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
        if(usageAsByte - fileSize < 0) {
            usageAsByte = 0L;
            return;
        }
        usageAsByte -= fileSize;
    }
}
