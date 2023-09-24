package ecsimsw.mymarket.service;

import ecsimsw.mymarket.domain.Product;
import ecsimsw.mymarket.domain.ProductRepository;
import ecsimsw.mymarket.dto.rest.ProductCursor;
import ecsimsw.mymarket.dto.rest.ProductFrameRequest;
import ecsimsw.mymarket.dto.rest.ProductResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static ecsimsw.mymarket.domain.ProductRepository.Specs.*;

@Transactional(readOnly = true)
@Service
public class ProductSearchService {

    private final ProductRepository productRepository;

    public ProductSearchService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> fetchFrame(Pageable pageable, ProductFrameRequest frameRequest) {
        final Sort sort = pageable.getSort();
        final Specification<Product> filters = frameRequest.searchFilters();
        final List<Product> products = productRepository.fetchAll(filters, sort, pageable.getPageSize());
        return ProductResponse.listOf(products);
    }

    public List<ProductResponse> fetchNextFrame(Pageable pageable, ProductFrameRequest frameRequest) {
        final Sort sort = pageable.getSort();
        final Specification<Product> filters = fetchFilters(frameRequest, sort);
        final List<Product> products = productRepository.fetchAll(filters, sort, pageable.getPageSize());
        return ProductResponse.listOf(products);
    }

    public List<ProductResponse> fetchPrevFrame(Pageable pageable, ProductFrameRequest frameRequest) {
        final Sort reversedSort = reverseDirections(pageable.getSort());
        final Specification<Product> filters = fetchFilters(frameRequest, reversedSort);
        final List<Product> products = productRepository.fetchAll(filters, reversedSort, pageable.getPageSize());
        return ProductResponse.reverseListOf(products);
    }

    private static Sort reverseDirections(Sort sort) {
        return Sort.by(sort.stream()
            .map(it -> new Sort.Order(it.isAscending() ? Sort.Direction.DESC : Sort.Direction.ASC, it.getProperty()))
            .collect(Collectors.toList()));
    }

    private Specification<Product> fetchFilters(ProductFrameRequest frameRequest, Sort sort) {
        final ProductCursor cursor = frameRequest.cursor();
        final Specification<Product> searchFilters = frameRequest.searchFilters();
        for (Sort.Order order : sort.toSet()) {
            if (order.getProperty().equals("name")) {
                return searchFilters.and(compareByName(order.getDirection(), cursor.name())
                    .or(equalsName(cursor.name()).and(compareId(order.getDirection(), cursor.id()))));
            }
            if (order.getProperty().equals("price")) {
                return searchFilters.and(compareByPrice(order.getDirection(), cursor.price())
                    .or(equalsPrice(cursor.price()).and(compareId(order.getDirection(), cursor.id()))));
            }
        }
        final Sort.Order defaultOrder = sort.getOrderFor("id");
        if (defaultOrder != null) {
            return searchFilters.and(compareId(defaultOrder.getDirection(), cursor.id()));
        }
        return searchFilters;
    }
}
