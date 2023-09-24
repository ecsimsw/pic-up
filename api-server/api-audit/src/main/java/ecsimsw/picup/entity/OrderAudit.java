package ecsimsw.picup.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "order-audit")
public class OrderAudit {

    @Id
    private String auditId;

    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static OrderAudit from(String content) {
        return new OrderAudit(null, content);
    }

    public OrderAudit() {
    }

    public OrderAudit(String auditId, String content) {
        this.auditId = auditId;
        this.content = content;
    }

    public String getAuditId() {
        return auditId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
