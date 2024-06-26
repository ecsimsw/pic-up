package ecsimsw.picup.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
public class MemberEvent {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long limitAsBytes;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private MemberEventType type;

    private final LocalDateTime createdAt = LocalDateTime.now();

    public MemberEvent(Long id, long userId, long limitAsBytes, MemberEventType type) {
        if(limitAsBytes < 0) {
            throw new IllegalArgumentException("Invalid sign up event");
        }
        this.id = id;
        this.userId = userId;
        this.limitAsBytes = limitAsBytes;
        this.type = type;
    }

    public MemberEvent(long userId, long limitAsBytes, MemberEventType type) {
        this(null, userId, limitAsBytes, type);
    }

    public static MemberEvent created(long userId, long limitAsBytes) {
        return new MemberEvent(userId, limitAsBytes, MemberEventType.CREATED);
    }

    public static MemberEvent deleted(long userId) {
        return new MemberEvent(userId, 0L, MemberEventType.DELETED);
    }

    public boolean isDeletionEvent() {
        return type == MemberEventType.DELETED;
    }

    public boolean isSignUpEvent() {
        return type == MemberEventType.CREATED;
    }
}

enum MemberEventType {
    CREATED, DELETED
}

