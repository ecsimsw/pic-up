package ecsimsw.picup.domain;

import lombok.Getter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Entity
public class UserFolder {

    public final static UserFolder ROOT = new UserFolder(null, null, "/");

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

//    private Long userId

    @NotNull
    private Long parentId;

    @NotNull
    private String name;

    public UserFolder() {
    }

    public UserFolder(Long id, Long parentId, String name) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
    }

    public UserFolder(Long parentId, String name) {
        this(null, parentId, name);
    }
}
