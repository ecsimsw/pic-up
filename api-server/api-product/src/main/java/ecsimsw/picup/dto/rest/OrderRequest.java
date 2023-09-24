package ecsimsw.picup.dto.rest;

import ecsimsw.picup.domain.Order;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderRequest {

    private Long productId;
    private Long userId;
    private int quantity;

    public OrderRequest() {
    }

    public OrderRequest(Long productId, Long userId, int quantity) {
        this.productId = productId;
        this.userId = userId;
        this.quantity = quantity;
    }

    public Order toEntity() {
        return new Order(userId, productId, quantity);
    }
}
