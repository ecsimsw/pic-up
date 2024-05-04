package ecsimsw.picup.album.domain;

import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface StorageResourceRepository extends JpaRepository<StorageResource, Long> {

    Optional<StorageResource> findByStorageTypeAndResourceKey(StorageType type, ResourceKey resourceKey);

    @Modifying
    @Query("SELECT resource FROM StorageResource resource " +
            "WHERE resource.createdAt < :expiration AND resource.toBeDeleted = true")
    List<StorageResource> findAllToBeDeletedCreatedBefore(
        @Param("expiration") LocalDateTime expiration
    );

    @Modifying
    @Query("UPDATE StorageResource resource SET resource.toBeDeleted = true " +
        "WHERE resource.resourceKey IN (:resourceKeys)")
    void updateAllToBeDeleted(List<ResourceKey> resourceKeys);
}