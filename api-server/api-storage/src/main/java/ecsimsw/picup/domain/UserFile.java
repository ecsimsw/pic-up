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
    private Long resourceSize;

    @NotNull
    private String resourceKey;

    public UserFile() {
    }

    public UserFile(Long id, Long folderId, String name, Long resourceSize, String resourceKey) {
        this.id = id;
        this.folderId = folderId;
        this.name = name;
        this.resourceSize = resourceSize;
        this.resourceKey = resourceKey;
    }

    public UserFile(Long folderId, String name, Long resourceSize, String resourceKey) {
        this(null, folderId, name, resourceSize, resourceKey);
    }
}
