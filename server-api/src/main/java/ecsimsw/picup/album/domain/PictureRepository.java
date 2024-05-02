package ecsimsw.picup.album.domain;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PictureRepository extends JpaRepository<Picture, Long> {

    List<Picture> findAllByAlbumId(Long albumId);

    @Query("SELECT picture FROM Picture picture JOIN FETCH picture.album " +
        "WHERE picture.album.id = :albumId AND picture.fileResource.resourceKey = :resourceKey AND picture.isDeleted = false")
    Optional<Picture> findByResourceKey(
        @Param("resourceKey") String resourceKey
    );

    @Query("SELECT picture FROM Picture picture JOIN FETCH picture.album " +
        "WHERE picture.album.id = :albumId AND picture.createdAt < :createdAt AND picture.isDeleted = false")
    List<Picture> findAllByAlbumOrderThan(
        @Param("albumId") Long albumId,
        @Param("createdAt") LocalDateTime createdAt,
        PageRequest pageRequest
    );
}
