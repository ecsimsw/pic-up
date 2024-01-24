package ecsimsw.picup.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface StorageUsageRepository extends JpaRepository<StorageUsage, Long> {

    @Lock(LockModeType.OPTIMISTIC)
    Optional<StorageUsage> findByUserId(Long userId);

    StorageUsage getReferenceByUserId(Long userId);
}
