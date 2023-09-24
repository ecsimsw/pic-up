package ecsimsw.picup.controller;

import ecsimsw.picup.domain.Product;
import ecsimsw.picup.domain.ProductRepository;
import ecsimsw.picup.dto.rest.ProductCreationRequest;
import ecsimsw.picup.dto.rest.ProductResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/products/admin")
@RestController
public class ProductAdminController {

    private final ProductRepository productRepository;

    public ProductAdminController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> findAll() {
        final List<Product> all = productRepository.findAll();
        return ResponseEntity.ok(ProductResponse.listOf(all));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestBody ProductCreationRequest request) {
        final Product saved = productRepository.save(request.toEntity());
        return ResponseEntity.ok(ProductResponse.of(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
