package ecsimsw.picup.domain;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findAllByOrderByOrderNumber();

    Slice<Album> findAllByUserId(Long userId, Pageable pageable);
}
