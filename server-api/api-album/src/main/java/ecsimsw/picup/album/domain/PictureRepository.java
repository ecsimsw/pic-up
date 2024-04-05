package ecsimsw.picup.album.domain;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PictureRepository extends JpaRepository<Picture, Long> {

    List<Picture> findAllByAlbumId(Long albumId);

    @Query(value =
        "select picture from Picture picture " +
            "JOIN FETCH picture.album " +
            "where picture.album.id = :albumId and picture.createdAt < :createdAt"
    )
    List<Picture> findAllByAlbumOrderThan(
        @Param("albumId") Long albumId,
        @Param("createdAt") LocalDateTime createdAt,
        PageRequest pageRequest
    );
}
