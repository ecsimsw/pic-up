package ecsimsw.picup.album.domain;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FilePreUploadEventRepository extends JpaRepository<FilePreUploadEvent, Long> {

    @Query("DELETE FROM FilePreUploadEvent event " +
        "WHERE event.createdAt > :expiration")
    List<FilePreUploadEvent> findAllByCreatedAtGreaterThan(LocalDateTime expiration);
}
