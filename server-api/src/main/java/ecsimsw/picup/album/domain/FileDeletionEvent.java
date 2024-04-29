package ecsimsw.picup.album.domain;

import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class FileDeletionEvent {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Embedded
    private ResourceKey resourceKey;

    @NotNull
    private int deleteFailedCounts;

    @NotNull
    private LocalDateTime creationTime = LocalDateTime.now();

    public FileDeletionEvent(ResourceKey resourceKey) {
        this(null, resourceKey, 0, LocalDateTime.now());
    }

    public void countFailed() {
        deleteFailedCounts++;
    }
}
