package ecsimsw.mymarket.dto.event;

import ecsimsw.mymarket.domain.Order;
import ecsimsw.mymarket.domain.Product;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrderCreatedEvent {

    private final Order order;
    private final Product product;
    private final LocalDateTime createdTime = LocalDateTime.now();

    public OrderCreatedEvent(Order order, Product product) {
        this.order = order;
        this.product = product;
    }
}
