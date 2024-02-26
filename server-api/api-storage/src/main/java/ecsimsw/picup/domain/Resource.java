package ecsimsw.picup.domain;

import ecsimsw.picup.auth.UnauthorizedException;
import ecsimsw.picup.exception.InvalidResourceException;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ecsimsw.picup.storage.ImageStorage;
import ecsimsw.picup.storage.StorageKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Document(collection = "resource")
public class Resource {

    @Id
    private String resourceKey;

    private Long userId;

    private List<StorageKey> storedStorages = new ArrayList<>();

    private LocalDateTime createRequested;

    private LocalDateTime deleteRequested;

    public static Resource createRequested(Long userId, String tag, MultipartFile file) {
        final String resourceKey = ResourceKeyStrategy.generate(tag, file);
        return new Resource(resourceKey, userId, new ArrayList<>(), LocalDateTime.now(), null);
    }

    public void storedTo(ImageStorage storage) {
        storedTo(storage.key());
    }

    public void storedTo(StorageKey storageKey) {
        storedStorages = new ArrayList<>(storedStorages);
        storedStorages.add(storageKey);
    }

    public void deletedFrom(ImageStorage storage) {
        deletedFrom(storage.key());
    }

    public void deletedFrom(StorageKey storageKey) {
        storedStorages = new ArrayList<>(storedStorages);
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

    public boolean isStoredAt(ImageStorage storage) {
        return storedStorages.contains(storage.key());
    }

    public void requireSameUser(Long userId) {
        if(!this.userId.equals(userId)) {
            throw new UnauthorizedException("Unauthorized request");
        }
    }

    public void requireLived() {
        if (!isLived()) {
            throw new InvalidResourceException("Invalid resource");
        }
    }

    public void requireStoredAt(ImageStorage storage) throws FileNotFoundException {
        if(!isStoredAt(storage)) {
            throw new FileNotFoundException("Not exists resource");
        }
    }
}
