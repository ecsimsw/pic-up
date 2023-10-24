package ecsimsw.picup.domain;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.mapping.Document;

@EnableMongoAuditing
@Getter
@Document(collection = "history")
public class History {

    @Id
    private String historyId;

    private Map<String, Object> content;

    @CreatedDate
    private LocalDateTime createdAt;

    public History() {
    }

    public History(String historyId, Map<String, Object> content) {
        this.historyId = historyId;
        this.content = content;
    }

    public History(Map<String, Object> content) {
        this(null, content);
    }
}
