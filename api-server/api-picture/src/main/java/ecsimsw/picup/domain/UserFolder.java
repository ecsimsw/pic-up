package ecsimsw.picup.domain;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
public class UserFolder {

    public final static UserFolder ROOT = new UserFolder(null, null, "/");

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

//    private Long userId

    @OneToOne(fetch = FetchType.LAZY)
    private UserFolder parentFolder;

    private String name;

    public UserFolder() {
    }

    public UserFolder(Long id, UserFolder parentFolder, String name) {
        this.id = id;
        this.parentFolder = parentFolder;
        this.name = name;
    }

    public UserFolder(UserFolder parentFolder, String name) {
        this(null, parentFolder, name);
    }
}
