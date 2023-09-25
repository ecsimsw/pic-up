package ecsimsw.picup.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Entity
public class ResourceKey {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull
    private Long userFileId;

    @NotNull
    private String key;

    // TODO :: Storage type, file type

    public ResourceKey() {
    }

    public ResourceKey(Long id, Long userFileId, String key) {
        this.id = id;
        this.userFileId = userFileId;
        this.key = key;
    }

    public ResourceKey(Long userFileId, String key) {
        this(null, userFileId, key);
    }
}
