package ecsimsw.picup.album.domain;

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
        return pictures.stream()
            .map(picture -> new FileDeletionEvent(userId, picture.getResourceKey()))
            .collect(Collectors.toList());
    }
}
