package ecsimsw.picup.domain;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PictureRepository extends JpaRepository<Picture, Long>,
    JpaSpecificationExecutor<Picture> {

    List<Picture> findAllByAlbumId(Long albumId);

    Slice<Picture> findAllByAlbumId(Long albumId, Pageable pageable);

    interface Specification {

    }
}
