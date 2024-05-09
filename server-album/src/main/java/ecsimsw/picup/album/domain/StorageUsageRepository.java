package ecsimsw.picup.album.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageUsageRepository extends JpaRepository<StorageUsage, Long> {

    Optional<StorageUsage> findByUserId(Long userId);
}
