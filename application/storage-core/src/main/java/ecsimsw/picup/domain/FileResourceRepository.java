package ecsimsw.picup.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileResourceRepository extends JpaRepository<FileResource, Long> {

    Optional<FileResource> findByStorageTypeAndResourceKey(StorageType type, ResourceKey resourceKey);

    @Query("SELECT resource FROM FileResource resource " +
        "WHERE resource.createdAt < :expiration AND resource.toBeDeleted = true")
    List<FileResource> findAllToBeDeletedCreatedBefore(
        @Param("expiration") LocalDateTime expiration,
        PageRequest pageRequest
    );

    @Modifying
    @Query("UPDATE FileResource resource SET resource.toBeDeleted = true " +
        "WHERE resource.resourceKey IN :resourceKeys")
    void setAllToBeDeleted(
        @Param("resourceKeys") List<ResourceKey> resourceKeys
    );

    @Query("UPDATE FileResource resource SET resource.toBeDeleted = true " +
        "WHERE resource.resourceKey = :resourceKey")
    void setToBeDeleted(
        @Param("resourceKey") ResourceKey resourceKey
    );
}
