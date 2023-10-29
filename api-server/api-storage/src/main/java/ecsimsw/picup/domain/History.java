package ecsimsw.picup.domain;

import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "history")
public class History {

    @Id
    private String historyId;

    private HistoryType historyType;

    private String resourceKey;

    private String note;

    private final LocalDateTime historyAt = LocalDateTime.now();

    public History() {
    }

    public History(String historyId, HistoryType historyType, String resourceKey, String note) {
        this.historyId = historyId;
        this.historyType = historyType;
        this.resourceKey = resourceKey;
        this.note = note;
    }

    public History(HistoryType historyType, String resourceKey, String note) {
        this(null, historyType, resourceKey, note);
    }

    public static History create(String resourceKey, String note) {
        return new History(HistoryType.CREATE, resourceKey, note);
    }

    public static History create(String resourceKey) {
        return new History(HistoryType.CREATE, resourceKey, null);
    }

    public static History delete(String resourceKey, String note) {
        return new History(HistoryType.DELETE, resourceKey, note);
    }

    public static History delete(String resourceKey) {
        return new History(HistoryType.DELETE, resourceKey, null);
    }
}
