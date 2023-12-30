package ecsimsw.picup.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FileDeletionEventOutbox extends JpaRepository<FileDeletionEvent, Long> {
}
