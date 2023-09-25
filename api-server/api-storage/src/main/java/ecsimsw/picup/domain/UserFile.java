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

    @OneToOne(fetch = FetchType.LAZY)
    private UserFolder folder;

    @NotNull
    private String name;

    @NotNull
    private String resourceKey;

    public UserFile() {
    }

    public UserFile(Long id, UserFolder folder, String name, String resourceKey) {
        this.id = id;
        this.folder = folder;
        this.name = name;
        this.resourceKey = resourceKey;
    }

    public UserFile(UserFolder folder, String name, String resourceKey) {
        this(null, folder, name, resourceKey);
    }

    public Long getFolderId() {
        return folder.getId();
    }
}
