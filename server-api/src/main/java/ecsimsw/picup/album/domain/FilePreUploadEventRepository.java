package ecsimsw.picup.album.domain;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FilePreUploadEventRepository extends JpaRepository<FilePreUploadEvent, String> {

    @Modifying
    @Query(
        "DELETE FROM FilePreUploadEvent event " +
        "WHERE event.createdAt > :expiration"
    )
    void findAllByCreatedAtGreaterThan(
        @Param("expiration") LocalDateTime expiration
    );
}
