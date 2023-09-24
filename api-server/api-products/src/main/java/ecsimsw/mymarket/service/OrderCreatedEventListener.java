package ecsimsw.mymarket.service;

import ecsimsw.mymarket.dto.event.OrderCreatedEvent;
import ecsimsw.mymarket.kafka.MQProducer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderCreatedEventListener {

    private final MQProducer<OrderCreatedEvent> orderMQProducer;

    public OrderCreatedEventListener(MQProducer<OrderCreatedEvent> mqProducer) {
        this.orderMQProducer = mqProducer;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendMessageQueue(OrderCreatedEvent orderCreatedEvent) {
        orderMQProducer.send(orderCreatedEvent);
    }
}
