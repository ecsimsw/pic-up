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

    private String thumbnailImage;

    public Album() {
    }

    public Album(Long id, String name, String thumbnailImage) {
        this.id = id;
        this.name = name;
        this.thumbnailImage = thumbnailImage;
    }

    public Album(String name, String thumbnailImage) {
        this(null, name, thumbnailImage);
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateThumbnailImage(String thumbnailImage) {
        this.thumbnailImage = thumbnailImage;
    }
}
