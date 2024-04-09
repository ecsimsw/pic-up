package ecsimsw.picup.album.domain;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.List;

public interface FileDeletionEventOutbox extends JpaRepository<FileDeletionEvent, Long> {

    List<FileDeletionEvent> findAll(Sort sort);
}
