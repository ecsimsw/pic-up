package ecsimsw.picup.domain;

import ecsimsw.picup.auth.UnauthorizedException;
import ecsimsw.picup.ecrypt.AES256Converter;
import java.time.LocalDateTime;
import javax.persistence.Convert;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Getter
@Entity
public class Album {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull
    private Long userId;

    @Convert(converter = AES256Converter.class)
    private String name;

    @Convert(converter = AES256Converter.class)
    private String resourceKey;

    private LocalDateTime createdAt;

    public Album() {
    }

    public Album(Long id, Long userId, String name, String resourceKey, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.resourceKey = resourceKey;
        this.createdAt = createdAt;
    }

    public Album(Long userId, String name, String resourceKey, LocalDateTime createdAt) {
        this(null, userId, name, resourceKey, createdAt);
    }

    public Album(Long userId, String name, String resourceKey) {
        this(null, userId, name, resourceKey, LocalDateTime.now());
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateThumbnail(String thumbnailImage) {
        this.resourceKey = thumbnailImage;
    }

    public void authorize(Long userId) {
        if(!this.userId.equals(userId)) {
            throw new UnauthorizedException("User doesn't have permission on this album");
        }
    }
}
