package ecsimsw.picup.domain;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Entity
public class Album {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private String name;
    private String thumbnailResourceKey;

    public Album() {
    }

    public Album(Long id, String name, String thumbnailResourceKey) {
        this.id = id;
        this.name = name;
        this.thumbnailResourceKey = thumbnailResourceKey;
    }

    public Album(String name, String thumbnailResourceKey) {
        this(null, name, thumbnailResourceKey);
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateThumbnail(String thumbnailImage) {
        this.thumbnailResourceKey = thumbnailImage;
    }
}
