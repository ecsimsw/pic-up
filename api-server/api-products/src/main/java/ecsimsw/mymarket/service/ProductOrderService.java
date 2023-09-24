package ecsimsw.mymarket.service;

import ecsimsw.mymarket.domain.Order;
import ecsimsw.mymarket.domain.OrderRepository;
import ecsimsw.mymarket.domain.Product;
import ecsimsw.mymarket.domain.ProductRepository;
import ecsimsw.mymarket.dto.event.OrderCreatedEvent;
import ecsimsw.mymarket.dto.rest.OrderRequest;
import ecsimsw.mymarket.dto.rest.OrderResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductOrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProductOrderService(ProductRepository productRepository, OrderRepository orderRepository, ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderResponse order(OrderRequest orderRequest) {
        final Order order = orderRequest.toEntity();
        final Product product = productRepository.findById(order.getProductId()).orElseThrow();
        product.deduce(order.getQuantity());

        final Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderCreatedEvent(order, product));
        return OrderResponse.of(saved);
    }
}
