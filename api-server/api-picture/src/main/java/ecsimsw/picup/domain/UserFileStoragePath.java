package ecsimsw.picup.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Entity
public class UserFileStoragePath {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull
    private Long userFileId;

    @NotNull
    private String resourceKey;

    // TODO :: Storage type, file type

    public UserFileStoragePath() {
    }

    public UserFileStoragePath(Long id, Long userFileId, String resourceKey) {
        this.id = id;
        this.userFileId = userFileId;
        this.resourceKey = resourceKey;
    }

    public UserFileStoragePath(Long userFileId, String resourceKey) {
        this(null, userFileId, resourceKey);
    }
}
