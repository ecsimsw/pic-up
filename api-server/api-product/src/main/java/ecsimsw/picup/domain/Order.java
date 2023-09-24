package ecsimsw.picup.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

@Getter
@Table(name = "orders")
@Entity
public class Order {

    @CreatedDate
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private Long userId;
    private Long productId;
    private int quantity;

    public Order() {
    }

    public Order(Long id, Long userId, Long productId, int quantity) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public Order(Long userId, Long productId, int quantity) {
        this(null, userId, productId, quantity);
    }
}
