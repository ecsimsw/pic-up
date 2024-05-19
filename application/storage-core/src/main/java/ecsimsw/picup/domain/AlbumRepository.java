package ecsimsw.picup.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long>, JpaSpecificationExecutor<Album> {

    Optional<Album> findByIdAndUserId(Long albumId, Long userId);

    List<Album> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
