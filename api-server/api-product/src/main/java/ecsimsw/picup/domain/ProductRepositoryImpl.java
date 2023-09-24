package ecsimsw.picup.domain;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Transactional
@Repository
public class ProductRepositoryImpl  extends SimpleJpaRepository<Product, Long> implements ProductRepository  {

    public ProductRepositoryImpl(EntityManager entityManager) {
        super(Product.class, entityManager);
    }

    @Override
    public List<Product> fetchAll(Specification<Product> specification, Sort sort, int limit) {
        final TypedQuery<Product> query = getQuery(specification, sort);
        query.setFirstResult(0);
        query.setMaxResults(limit);
        return query.getResultList();
    }
}
