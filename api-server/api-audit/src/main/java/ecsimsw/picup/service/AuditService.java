package ecsimsw.picup.service;

import ecsimsw.picup.entity.OrderAudit;
import ecsimsw.picup.entity.OrderAuditRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final OrderAuditRepository orderAuditRepository;

    public AuditService(OrderAuditRepository orderAuditRepository) {
        this.orderAuditRepository = orderAuditRepository;
    }

    @KafkaListener(topics = "${mymarket.order.kafka.topic.name}", groupId = "${mymarket.order.kafka.group.id}")
    public void listenWithHeaders(@Payload String orderEvent) {
        orderAuditRepository.save(OrderAudit.from(orderEvent));
    }
}
