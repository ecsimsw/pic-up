package ecsimsw.mymarket.controller;

import ecsimsw.mymarket.entity.OrderAudit;
import ecsimsw.mymarket.entity.OrderAuditRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderAuditController {

    private final OrderAuditRepository orderAuditRepository;

    public OrderAuditController(OrderAuditRepository orderAuditRepository) {
        this.orderAuditRepository = orderAuditRepository;
    }

    @GetMapping("/audits")
    public ResponseEntity<List<OrderAudit>> findAll() {
        return ResponseEntity.ok(orderAuditRepository.findAll());
    }
}
