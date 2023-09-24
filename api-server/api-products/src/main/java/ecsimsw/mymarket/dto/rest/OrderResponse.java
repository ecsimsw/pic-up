package ecsimsw.mymarket.dto.rest;

import ecsimsw.mymarket.domain.Order;
import lombok.Getter;

@Getter
public class OrderResponse {

    private Long id;
    private Long userId;
    private Long productId;
    private int quantity;

    public OrderResponse() {
    }

    public OrderResponse(Long id, Long userId, Long productId, int quantity) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public static OrderResponse of(Order order) {
        return new OrderResponse(order.getId(), order.getUserId(), order.getProductId(), order.getQuantity());
    }
}
