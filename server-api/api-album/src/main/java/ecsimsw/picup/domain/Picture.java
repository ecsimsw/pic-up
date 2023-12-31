package ecsimsw.picup.domain;

import ecsimsw.picup.ecrypt.AES256Converter;
import java.time.LocalDateTime;
import javax.persistence.Convert;

import ecsimsw.picup.exception.AlbumException;
import javax.validation.constraints.NotNull;
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

    @NotNull
    private Long albumId;

    @Convert(converter = AES256Converter.class)
    private String resourceKey;

    private Long fileSize;

    @Convert(converter = AES256Converter.class)
    private String description;

    private LocalDateTime createdAt;

    public Picture() {
    }

    public Picture(Long id, Long albumId, String resourceKey, Long fileSize, String description, LocalDateTime createdAt) {
        this.id = id;
        this.albumId = albumId;
        this.resourceKey = resourceKey;
        this.fileSize = fileSize;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Picture(Long albumId, String resourceKey, Long fileSize, String description) {
        this(null, albumId, resourceKey, fileSize, description, LocalDateTime.now());
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
