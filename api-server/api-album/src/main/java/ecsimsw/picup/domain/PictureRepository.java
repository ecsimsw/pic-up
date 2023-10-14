package ecsimsw.picup.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PictureRepository extends JpaRepository<Picture, Long> {

    Optional<Picture> findTopByAlbumIdOrderByOrderNumber(Long albumId);

    List<Picture> findAllByAlbumId(Long albumId);
}
