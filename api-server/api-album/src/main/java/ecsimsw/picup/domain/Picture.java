package ecsimsw.picup.domain;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Entity
public class Picture {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private Long albumId;

    private String resourceKey;

    private String name;

    private String description;

    public Picture() {
    }

    public Picture(Long id, Long albumId, String resourceKey, String name, String description) {
        this.id = id;
        this.albumId = albumId;
        this.resourceKey = resourceKey;
        this.name = name;
        this.description = description;
    }

    public Picture(Long albumId, String resourceKey, String name, String description) {
        this(null, albumId, resourceKey, name, description);
    }
}
