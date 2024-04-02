package ecsimsw.picup.album.domain;

import java.util.LinkedList;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
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

    public FileDeletionEvent(Long userId, String resourceKey) {
        this(null, userId, resourceKey, LocalDateTime.now());
    }

    public static List<FileDeletionEvent> listOf(Long userId, List<Picture> pictures) {
        var deletionEvents = new LinkedList<FileDeletionEvent>();
        for(var picture : pictures) {
            deletionEvents.add(new FileDeletionEvent(userId, picture.getResourceKey()));
            deletionEvents.add(new FileDeletionEvent(userId, picture.getThumbnailResourceKey()));
        }
        return deletionEvents;
    }
}
