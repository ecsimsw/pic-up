package ecsimsw.picup.domain;

import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "history")
public class History {

    @Id
    private String historyId;

    private StorageKey storageKey;

    private HistoryType historyType;

    private String resourceKey;

    private String note;

    @CreatedDate
    private LocalDateTime createdAt;

    public History() {
    }

    public History(String historyId, StorageKey storageKey, HistoryType historyType, String resourceKey, String note) {
        this.historyId = historyId;
        this.storageKey = storageKey;
        this.historyType = historyType;
        this.resourceKey = resourceKey;
        this.note = note;
    }

    public History(StorageKey storageKey, HistoryType historyType, String resourceKey, String note) {
        this(null, storageKey, historyType, resourceKey, note);
    }

    public History(StorageKey storageKey, HistoryType historyType, String resourceKey) {
        this(null, storageKey, historyType, resourceKey, null);
    }

    public static History create(StorageKey storageKey, String resourceKey, String note) {
        return new History(storageKey, HistoryType.CREATE, resourceKey, note);
    }

    public static History create(StorageKey storageKey, String resourceKey) {
        return new History(storageKey, HistoryType.CREATE, resourceKey);
    }

    public static History delete(StorageKey storageKey, String resourceKey, String note) {
        return new History(storageKey, HistoryType.DELETE, resourceKey, note);
    }

    public static History delete(StorageKey storageKey, String resourceKey) {
        return new History(storageKey, HistoryType.DELETE, resourceKey);
    }
}
