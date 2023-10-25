package ecsimsw.picup.domain;

import ecsimsw.picup.exception.InvalidResourceException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "resource")
public class Resource {

    @Id
    private String resourceKey;

    private List<StorageKey> storedStorages;

    private LocalDateTime createRequested;

    private LocalDateTime deleteRequested;

    public Resource() {
    }

    public Resource(String resourceKey, List<StorageKey> storedStorages, LocalDateTime createRequested, LocalDateTime deletedAt) {
        this.resourceKey = resourceKey;
        this.storedStorages = storedStorages;
        this.createRequested = createRequested;
        this.deleteRequested = deletedAt;
    }

    public Resource(List<StorageKey> storedStorages, LocalDateTime createRequested, LocalDateTime deletedAt) {
        this.storedStorages = storedStorages;
        this.createRequested = createRequested;
        this.deleteRequested = deletedAt;
    }

    public static Resource createRequested(String resourceKey) {
        return new Resource(resourceKey, new ArrayList<>(), LocalDateTime.now(), null);
    }

    public void storedAt(StorageKey storageKey) {
        storedStorages.add(storageKey);
    }

    public void deletedAt(StorageKey storageKey) {
        storedStorages.remove(storageKey);
    }

    public void deleteRequested() {
        if(createRequested == null) {
            throw new InvalidResourceException("Never created resource");
        }
        this.deleteRequested = LocalDateTime.now();
    }

    public boolean isLived() {
        return createRequested != null && deleteRequested == null;
    }

    public boolean isStoredAt(StorageKey key) {
        return storedStorages.contains(key);
    }
}
