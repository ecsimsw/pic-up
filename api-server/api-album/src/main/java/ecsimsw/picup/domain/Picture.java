package ecsimsw.picup.domain;

import ecsimsw.picup.ecrypt.AES256Converter;
import javax.persistence.Convert;
import javax.persistence.Converter;
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

    @Convert(converter = AES256Converter.class)
    private String resourceKey;

    @Convert(converter = AES256Converter.class)
    private String description;

    private int orderNumber;

    public Picture() {
    }

    public Picture(Long id, Long albumId, String resourceKey, String description, int orderNumber) {
        this.id = id;
        this.albumId = albumId;
        this.resourceKey = resourceKey;
        this.description = description;
        this.orderNumber = orderNumber;
    }

    public Picture(Long albumId, String resourceKey, String description, int orderNumber) {
        this(null, albumId, resourceKey, description, orderNumber);
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateImage(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public void updateOrder(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void validateAlbum(Long albumId) {
        if(!this.albumId.equals(albumId)) {
            throw new IllegalArgumentException("Invalid album");
        }
    }
}
