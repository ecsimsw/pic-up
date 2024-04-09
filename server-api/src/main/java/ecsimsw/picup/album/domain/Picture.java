package ecsimsw.picup.album.domain;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(indexes = {
    @Index(name = "idx_albumId_createdAt_id", columnList = "albumId, createdAt")
})
@Entity
public class Picture {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotNull
    @JoinColumn(name="albumId", nullable=false)
    @ManyToOne
    private Album album;

    @NotBlank
    private String resourceKey;

    @NotBlank
    private String thumbnailResourceKey;

    @Min(0)
    private long fileSize;

    @NotNull
    private LocalDateTime createdAt;

    public Picture(Album album, String resourceKey, String thumbnailResourceKey, Long fileSize) {
        this(null, album, resourceKey, thumbnailResourceKey, fileSize, LocalDateTime.now());
    }

    public void checkSameUser(Long userId) {
        album.authorize(userId);
    }
}
