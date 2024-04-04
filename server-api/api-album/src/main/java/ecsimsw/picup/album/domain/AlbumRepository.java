package ecsimsw.picup.album.domain;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, Long>, JpaSpecificationExecutor<Album>, AlbumSpecRepository {

    Optional<Album> findByIdAndUserId(Long albumId, Long userId);

    Slice<Album> findAllByUserId(Long userId, Pageable pageable);

    List<Album> fetch(Specification<Album> specification, int limit, Sort sort);

    interface AlbumSearchSpecs {

        Sort ascByCreatedAt = Sort.by(Sort.Direction.ASC, Album_.CREATED_AT, Album_.ID);

        static Specification<Album> where() {
            return Specification.where(null);
        }

        static Specification<Album> where(Specification<Album> spec) {
            return where().and(spec);
        }

        static Specification<Album> createdLater(LocalDateTime createdAt, Long cursorAlbumId) {
            return createdLater(createdAt, cursorAlbumId)
                    .or(equalsCreatedTime(createdAt).and(lessId(cursorAlbumId))
            );
        }

        static Specification<Album> isUser(Long userId) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Album_.USER_ID), userId);
        }

        static Specification<Album> createdLater(LocalDateTime localDateTime) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get(Album_.CREATED_AT), localDateTime);
        }

        static Specification<Album> equalsCreatedTime(LocalDateTime localDateTime) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Album_.CREATED_AT), localDateTime);
        }

        static Specification<Album> lessId(Long id) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get(Album_.ID), id);
        }
    }
}
