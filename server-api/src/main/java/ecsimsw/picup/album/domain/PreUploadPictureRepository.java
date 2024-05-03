package ecsimsw.picup.album.domain;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PreUploadPictureRepository extends JpaRepository<PreUploadPicture, String> {

    @Modifying
    @Query(
        "DELETE FROM PreUploadPicture picture " +
        "WHERE picture.createdAt > :expiration"
    )
    void deleteAllCreatedAfter(
        @Param("expiration") LocalDateTime expiration
    );
}
