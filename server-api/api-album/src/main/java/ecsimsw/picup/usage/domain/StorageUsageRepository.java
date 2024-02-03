package ecsimsw.picup.usage.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface StorageUsageRepository extends JpaRepository<StorageUsage, Long> {

    @Lock(LockModeType.OPTIMISTIC)
    Optional<StorageUsage> findByUserId(Long userId);
}
