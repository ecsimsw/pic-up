package ecsimsw.picup.domain;

import ecsimsw.picup.ecrypt.AES256Converter;
import java.time.LocalDateTime;
import javax.persistence.Convert;
import javax.persistence.Converter;

import ecsimsw.picup.exception.AlbumException;
import javax.validation.constraints.NotNull;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

@Getter
@Entity
public class Picture {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull
    private Long albumId;

    @Convert(converter = AES256Converter.class)
    private String resourceKey;

    @Convert(converter = AES256Converter.class)
    private String description;

    private LocalDateTime createdAt;

    public Picture() {
    }

    public Picture(Long id, Long albumId, String resourceKey, String description) {
        this.id = id;
        this.albumId = albumId;
        this.resourceKey = resourceKey;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Picture(Long albumId, String resourceKey, String description) {
        this(null, albumId, resourceKey, description);
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateImage(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public void validateAlbum(Long albumId) {
        if(!this.albumId.equals(albumId)) {
            throw new AlbumException("Invalid album");
        }
    }
}
