package ecsimsw.picup.album.domain;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.data.domain.Sort;
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
    public List<Picture> fetch(Specification<Picture> specification, int limit, Sort sort) {
        final TypedQuery<Picture> query = getQuery(specification, sort);
        query.setFirstResult(0);
        query.setMaxResults(limit);
        return query.getResultList();
    }
}
