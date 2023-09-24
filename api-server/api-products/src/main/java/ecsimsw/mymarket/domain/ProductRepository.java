package ecsimsw.mymarket.domain;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> fetchAll(Specification<Product> specification, Sort sort, int limit);

    interface Specs {

        static Specification<Product> compareByName(Sort.Direction direction, String name) {
            if (direction.isAscending()) {
                return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get(Product_.name), name);
            } else {
                return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get(Product_.name), name);
            }
        }

        static Specification<Product> compareByPrice(Sort.Direction direction, int price) {
            if (direction.isAscending()) {
                return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get(Product_.price), price);
            } else {
                return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get(Product_.price), price);
            }
        }

        static Specification<Product> compareId(Sort.Direction direction, Long id) {
            if (direction.isAscending()) {
                return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get(Product_.id), id);
            } else {
                return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get(Product_.id), id);
            }
        }

        static Specification<Product> equalsName(String name) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Product_.name), name);
        }

        static Specification<Product> equalsPrice(int price) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Product_.price), price);
        }

        static Specification<Product> containsName(String name) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(Product_.name), "%" + name + "%");
        }

        static Specification<Product> greaterThanOrEqualsByPrice(int price) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get(Product_.price), price);
        }

        static Specification<Product> lessThanOrEqualsByPrice(int price) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get(Product_.price), price);
        }
    }
}
