package ecsimsw.picup.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StorageUsageRepository extends JpaRepository<StorageUsage, Long> {

    Optional<StorageUsage> findByUserId(Long userId);
}
