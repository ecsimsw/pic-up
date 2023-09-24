package ecsimsw.picup.domain;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
public class UserImageResource {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

//    private Long userId

    @OneToOne(fetch = FetchType.LAZY)
    private UserFolder parentFolder;

    private String name = "";
    private String storagePath = "";

    public UserImageResource() {
    }

    public UserImageResource(Long id, UserFolder parentFolder, String name, String storagePath) {
        this.id = id;
        this.parentFolder = parentFolder;
        this.name = name;
        this.storagePath = storagePath;
    }

    public UserImageResource(UserFolder parentFolder, String name, String storagePath) {
        this(null, parentFolder, name, storagePath);
    }
}
