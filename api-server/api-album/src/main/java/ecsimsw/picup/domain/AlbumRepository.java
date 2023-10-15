package ecsimsw.picup.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    Slice<Album> findAllByUserId(Long userId, Pageable pageable);
}
