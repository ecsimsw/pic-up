package ecsimsw.picup.domain;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AlbumRepository extends JpaRepository<Album, Long>, JpaSpecificationExecutor<Album>, AlbumSpecRepository {

    Slice<Album> findAllByUserId(Long userId, Pageable pageable);

    List<Album> fetch(Specification<Album> specification, Pageable pageable);

    interface AlbumSearchSpecs {

        static Specification<Album> where() {
            return Specification.where(null);
        }
        
        static Specification<Album> greaterOrder(int orderNumber) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get(Album_.ORDER_NUMBER), orderNumber);
        }
    }
}
