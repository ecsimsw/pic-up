package ecsimsw.picup.member.domain;

import ecsimsw.picup.album.exception.AlbumException;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    public StorageUsage(Long userId, long limitAsByte) {
        this.userId = userId;
        this.limitAsByte = limitAsByte;
        this.usageAsByte = 0L;
    }

    public boolean isAbleToStore(long addedSize) {
        return limitAsByte > usageAsByte + addedSize;
    }

    public void add(long addedSize) {
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
