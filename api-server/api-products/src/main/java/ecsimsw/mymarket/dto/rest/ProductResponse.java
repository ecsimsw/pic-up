package ecsimsw.mymarket.dto.rest;

import ecsimsw.mymarket.domain.Product;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ProductResponse {

    private Long id;
    private String name;
    private int price;
    private int quantity;

    public ProductResponse() {
    }

    public ProductResponse(Long id, String name, int price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public static List<ProductResponse> listOf(List<Product> products) {
        return products.stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public static List<ProductResponse> reverseListOf(List<Product> products) {
        Collections.reverse(products);
        return products.stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public static ProductResponse of(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getQuantity());
    }
}
