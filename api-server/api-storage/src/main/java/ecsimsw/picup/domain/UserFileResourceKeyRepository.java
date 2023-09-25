package ecsimsw.picup.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFileResourceKeyRepository extends JpaRepository<UserFileResourceKey, Long> {

    Optional<UserFileResourceKey> findByUserFileId(Long userFileId);

    void deleteByUserFileId(Long userFileId);
}
