package ecsimsw.picup.domain;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PictureRepository extends JpaRepository<Picture, Long>, JpaSpecificationExecutor<Picture>, PictureSpecRepository{

    List<Picture> findAllByAlbumId(Long albumId);

    Slice<Picture> findAllByAlbumId(Long albumId, Pageable pageable);

    List<Picture> fetch(Specification<Picture> specification, Pageable pageable);

    interface PictureSearchSpecs {

        static Specification<Picture> where() {
            return Specification.where(null);
        }

        static Specification<Picture> greaterOrderThan(int orderNumber) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get(Picture_.ORDER_NUMBER), orderNumber);
        }

        static Specification<Picture> isAlbum(long albumId) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Picture_.ALBUM_ID), albumId);
        }
    }
}
