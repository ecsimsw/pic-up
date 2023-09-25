package ecsimsw.picup.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFileStoragePathRepository extends JpaRepository<UserFileStoragePath, Long> {

    Optional<UserFileStoragePath> findByUserFileId(Long userFileId);
}
