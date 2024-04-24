package ecsimsw.picup.album.domain;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileDeletionEventRepository extends JpaRepository<FileDeletionEvent, Long> {

    List<FileDeletionEvent> findAll(Sort sort);
}
