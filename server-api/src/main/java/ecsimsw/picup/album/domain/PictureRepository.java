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
        "WHERE picture.fileResource = :resourceKey")
    Optional<Picture> findByResourceKey(
        @Param("resourceKey") ResourceKey resourceKey
    );

    @Query("SELECT picture FROM Picture picture JOIN FETCH picture.album " +
        "WHERE picture.album = :album AND picture.createdAt < :createdAt")
    List<Picture> findAllByAlbumOrderThan(
        @Param("album") Album album,
        @Param("createdAt") LocalDateTime createdAt,
        PageRequest pageRequest
    );
}
