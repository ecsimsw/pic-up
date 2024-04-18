package ecsimsw.picup.album.domain;

import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
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

    @NotNull
    private Long userId;

    @Embedded
    private ResourceKey resourceKey;

    @NotNull
    private LocalDateTime creationTime = LocalDateTime.now();

    public FileDeletionEvent(Long userId, ResourceKey resourceKey) {
        this(null, userId, resourceKey, LocalDateTime.now());
    }
}
