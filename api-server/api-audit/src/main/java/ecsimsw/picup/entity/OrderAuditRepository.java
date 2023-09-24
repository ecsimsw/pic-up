package ecsimsw.picup.entity;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderAuditRepository extends MongoRepository<OrderAudit, String> {

}
