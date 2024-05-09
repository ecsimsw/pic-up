package ecsimsw.picup.album.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface FileDeletionFailedLogRepository extends JpaRepository<FileDeletionFailedLog, LocalDateTime> {
}
