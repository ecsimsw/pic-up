package ecsimsw.picup.domain;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "residue")
public class Residue {

    @Id
    private String residueId;

    private String resourceKey;

    private StorageKey storageKey;

    private String note;

    @CreatedDate
    private LocalDateTime createdAt;

    public Residue() {
    }

    public Residue(String residueId, String resourceKey, StorageKey storageKey, String note) {
        this.residueId = residueId;
        this.resourceKey = resourceKey;
        this.storageKey = storageKey;
        this.note = note;
    }

    public Residue(String resourceKey, StorageKey storageKey, String note) {
        this(null, resourceKey, storageKey, note);
    }

    public static Residue from(String resourceKey, StorageKey storageKey, String message) {
        return new Residue(resourceKey, storageKey, message);
    }
}
