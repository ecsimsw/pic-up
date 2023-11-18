package ecsimsw.picup.domain;

import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PictureRepository extends JpaRepository<Picture, Long>, JpaSpecificationExecutor<Picture>, PictureSpecRepository {

    List<Picture> findAllByAlbumId(Long albumId);

    Slice<Picture> findAllByAlbumId(Long albumId, Pageable pageable);

    List<Picture> fetch(Specification<Picture> specification, int limit, Sort sort);

    interface PictureSearchSpecs {

        Sort sortByCreatedAtAsc = Sort.by(Sort.Direction.ASC, Picture_.CREATED_AT, Picture_.ID);

        static Specification<Picture> where() {
            return Specification.where(null);
        }

        static Specification<Picture> where(Specification<Picture> spec) {
            return Specification.<Picture>where(null).and(spec);
        }

        static Specification<Picture> createdLater(LocalDateTime createdAt) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get(Picture_.CREATED_AT), createdAt);
        }

        static Specification<Picture> equalsCreatedTime(LocalDateTime createdAt) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Picture_.CREATED_AT), createdAt);
        }

        static Specification<Picture> greaterId(long id) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get(Picture_.ID), id);
        }

        static Specification<Picture> isAlbum(long albumId) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Picture_.ALBUM_ID), albumId);
        }
    }
}
