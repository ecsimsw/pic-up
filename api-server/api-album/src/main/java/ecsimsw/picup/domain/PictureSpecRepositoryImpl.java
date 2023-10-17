package ecsimsw.picup.domain;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public class PictureSpecRepositoryImpl extends SimpleJpaRepository<Picture, Long> implements PictureSpecRepository {

    public PictureSpecRepositoryImpl(EntityManager entityManager) {
        super(Picture.class, entityManager);
    }

    @Override
    public List<Picture> fetch(Specification<Picture> specification, Pageable pageable) {
        final int limit = pageable.getPageSize();
        final TypedQuery<Picture> query = getQuery(specification, pageable.getSort());
        query.setFirstResult(0);
        query.setMaxResults(limit);
        return query.getResultList();
    }
}
