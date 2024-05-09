package ecsimsw.picup.album.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AlbumRepository extends JpaRepository<Album, Long>, JpaSpecificationExecutor<Album> {

    Optional<Album> findByIdAndUserId(Long albumId, Long userId);

    List<Album> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
