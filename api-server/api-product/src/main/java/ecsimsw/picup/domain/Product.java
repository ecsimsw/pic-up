package ecsimsw.picup.domain;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Entity
public class Product {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private String name;
    private int quantity;
    private int price;

    public Product() {
    }

    public Product(String name, int quantity, int price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public void deduce(int orderQuantity) {
        if(this.quantity - orderQuantity < 0) {
            throw new IllegalArgumentException("재고 부족");
        }
        this.quantity -= orderQuantity;
    }
}
