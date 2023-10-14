package ecsimsw.picup.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    Optional<Album> findTopByOrderByOrderNumber();

    List<Album> findAllByOrderByOrderNumber();
}
