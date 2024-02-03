package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.ecrypt.AES256Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
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
