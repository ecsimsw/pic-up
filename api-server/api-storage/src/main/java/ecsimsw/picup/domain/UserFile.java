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

    public UserFile() {
    }

    public UserFile(Long id, UserFolder folder, String name) {
        this.id = id;
        this.folder = folder;
        this.name = name;
    }

    public UserFile(UserFolder folder, String name) {
        this(null, folder, name);
    }

    public Long getFolderId() {
        return folder.getId();
    }
}
