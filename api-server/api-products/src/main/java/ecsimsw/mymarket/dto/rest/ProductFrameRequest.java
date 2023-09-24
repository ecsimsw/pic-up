package ecsimsw.mymarket.dto.rest;

import static ecsimsw.mymarket.domain.ProductRepository.Specs.containsName;
import static ecsimsw.mymarket.domain.ProductRepository.Specs.greaterThanOrEqualsByPrice;
import static ecsimsw.mymarket.domain.ProductRepository.Specs.lessThanOrEqualsByPrice;

import ecsimsw.mymarket.domain.Product;

import java.util.Optional;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

@Setter
@Getter
public class ProductFrameRequest {

    private Optional<String> containsName;
    private Optional<Integer> minPrice;
    private Optional<Integer> maxPrice;
    private Long cursorProductId;
    private String cursorProductName;
    private Integer cursorProductPrice;

    public ProductFrameRequest() {
    }

    public ProductFrameRequest(Optional<String> containsName, Optional<Integer> minPrice, Optional<Integer> maxPrice, Long cursorProductId, String cursorProductName, Integer cursorProductPrice) {
        this.containsName = containsName;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.cursorProductId = cursorProductId;
        this.cursorProductName = cursorProductName;
        this.cursorProductPrice = cursorProductPrice;
    }

    public ProductCursor cursor() {
        return new ProductCursor(cursorProductId, cursorProductName, cursorProductPrice);
    }

    public Specification<Product> searchFilters() {
        Specification<Product> spec = (root, query, criteriaBuilder) -> null;
        if (containsName.isPresent() && !containsName.get().isBlank()) {
            spec = spec.and(containsName(containsName.orElseThrow()));
        }
        if (minPrice.isPresent()) {
            spec = spec.and(greaterThanOrEqualsByPrice(minPrice.orElseThrow()));
        }
        if (maxPrice.isPresent()) {
            spec = spec.and(lessThanOrEqualsByPrice(maxPrice.orElseThrow()));
        }
        return spec;
    }
}
