package ecsimsw.picup.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Setter
@Getter
@Entity
public class FileDeletionEvent {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private Long userId;
    private String resourceKey;
    private LocalDateTime creationTime = LocalDateTime.now();

    public FileDeletionEvent() {
    }

    public FileDeletionEvent(Long id, Long userId, String resourceKey) {
        this.id = id;
        this.userId = userId;
        this.resourceKey = resourceKey;
    }

    public FileDeletionEvent(Long userId, String resourceKey) {
        this(null, userId, resourceKey);
    }

    public static List<FileDeletionEvent> listOf(Long userId, List<String> resourceKeys) {
        return resourceKeys.stream()
            .map(resourceKey -> new FileDeletionEvent(userId, resourceKey))
            .collect(Collectors.toList());
    }
}
