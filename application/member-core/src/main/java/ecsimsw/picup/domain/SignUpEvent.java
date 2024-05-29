package ecsimsw.picup.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
public class SignUpEvent {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long limitAsBytes;

    private LocalDateTime createdAt;

    public SignUpEvent(Long id, Long userId, Long limitAsBytes, LocalDateTime createdAt) {
        if(userId == null || limitAsBytes < 0 || createdAt == null) {
            throw new IllegalArgumentException("Invalid sign up event");
        }
        this.id = id;
        this.userId = userId;
        this.limitAsBytes = limitAsBytes;
        this.createdAt = createdAt;
    }

    public SignUpEvent(Long userId, Long limitAsBytes) {
        this(null, userId, limitAsBytes, LocalDateTime.now());
    }

    public static SignUpEvent from(Member member, long limitAsBytes) {
        return new SignUpEvent(
            member.getId(),
            limitAsBytes
        );
    }
}
