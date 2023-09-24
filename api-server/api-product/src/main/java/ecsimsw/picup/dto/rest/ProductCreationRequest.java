package ecsimsw.picup.dto.rest;

import ecsimsw.picup.domain.Product;
import lombok.Getter;

@Getter
public class ProductCreationRequest {

    private String name;
    private int quantity;
    private int price;

    public ProductCreationRequest() {
    }

    public ProductCreationRequest(String name, int quantity, int price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public Product toEntity() {
        return new Product(name, quantity, price);
    }
}
