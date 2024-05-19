package ecsimsw.picup.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import ecsimsw.picup.exception.StorageException;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
public class StorageUsage {

    @Id
    private Long userId;

    @Column(nullable = false)
    private long limitAsByte;

    @Column(nullable = false)
    private long usageAsByte;

    public StorageUsage(Long userId, long limitAsByte, long usageAsByte) {
        if (usageAsByte < 0 || limitAsByte < usageAsByte) {
            throw new StorageException("Invalid storage usage");
        }
        this.userId = userId;
        this.limitAsByte = limitAsByte;
        this.usageAsByte = usageAsByte;
    }

    public StorageUsage(Long userId, long limitAsByte) {
        this(userId, limitAsByte, 0L);
    }

    public boolean isAbleToStore(long addedSize) {
        return limitAsByte > usageAsByte + addedSize;
    }

    public void add(long addedSize) {
        if (!isAbleToStore(addedSize)) {
            throw new StorageException("Not available to store");
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
