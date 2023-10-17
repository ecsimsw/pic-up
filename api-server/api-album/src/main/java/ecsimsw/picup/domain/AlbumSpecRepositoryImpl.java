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
public class AlbumSpecRepositoryImpl extends SimpleJpaRepository<Album, Long> implements AlbumSpecRepository {

    public AlbumSpecRepositoryImpl(EntityManager entityManager) {
        super(Album.class, entityManager);
    }

    @Override
    public List<Album> fetch(Specification<Album> specification, Pageable pageable) {
        final int limit = pageable.getPageSize();
        final TypedQuery<Album> query = getQuery(specification, pageable.getSort());
        query.setFirstResult(0);
        query.setMaxResults(limit);
        return query.getResultList();
    }
}
