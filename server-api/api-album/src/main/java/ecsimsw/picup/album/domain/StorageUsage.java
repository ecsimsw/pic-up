package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class StorageUsage {

    @Id
    private Long userId;
    private long limitAsByte;
    private long usageAsByte;

    @Version
    private Timestamp versionStamp;

    public StorageUsage(Long userId, long limitAsByte, long usageAsByte) {
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
