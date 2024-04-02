package ecsimsw.picup.domain;

import ecsimsw.picup.auth.UnauthorizedException;
import ecsimsw.picup.storage.ImageStorage;
import ecsimsw.picup.storage.StorageKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class Resource {

    @Id
    private String resourceKey;

    @NotNull
    private Long userId;

    @Convert(converter = ListStorageKeyConverter.class)
    private List<StorageKey> storedStorages = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();

    public Resource(Long userId, String resourceKey) {
        this.resourceKey = resourceKey;
        this.userId = userId;
    }

    public void storedTo(StorageKey storageKey) {
        storedStorages = new ArrayList<>(storedStorages);
        storedStorages.add(storageKey);
    }

    public void deletedFrom(ImageStorage storage) {
        storedStorages = new ArrayList<>(storedStorages);
        storedStorages.remove(storage.key());
    }

    public void validateAccess(Long userId) {
        if (!this.userId.equals(userId)) {
            throw new UnauthorizedException("Unauthorized request");
        }
    }

    public boolean isNotStored() {
        return storedStorages.isEmpty();
    }
}
