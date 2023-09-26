package ecsimsw.picup.domain;

import javax.validation.constraints.NotNull;
import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
public class UserFile {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

//    private Long userId

    @NotNull
    private Long folderId;

    @NotNull
    private String name;

    @NotNull
    private String resourceKey;

    public UserFile() {
    }

    public UserFile(Long id, Long folderId, String name, String resourceKey) {
        this.id = id;
        this.folderId = folderId;
        this.name = name;
        this.resourceKey = resourceKey;
    }

    public UserFile(Long folderId, String name, String resourceKey) {
        this(null, folderId, name, resourceKey);
    }
}
