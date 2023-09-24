package ecsimsw.picup.service;

import ecsimsw.picup.domain.Order;
import ecsimsw.picup.domain.OrderRepository;
import ecsimsw.picup.domain.Product;
import ecsimsw.picup.domain.ProductRepository;
import ecsimsw.picup.dto.event.OrderCreatedEvent;
import ecsimsw.picup.dto.rest.OrderRequest;
import ecsimsw.picup.dto.rest.OrderResponse;
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
