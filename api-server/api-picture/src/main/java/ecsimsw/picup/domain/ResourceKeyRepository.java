package ecsimsw.picup.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceKeyRepository extends JpaRepository<ResourceKey, Long> {

    Optional<ResourceKey> findByUserFileId(Long userFileId);
}
