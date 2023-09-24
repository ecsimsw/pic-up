package ecsimsw.picup.kafka;

import ecsimsw.picup.dto.event.OrderCreatedEvent;
import ecsimsw.picup.logging.CustomLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
public class KafkaOrderProducer implements MQProducer<OrderCreatedEvent> {

    private static final CustomLogger LOGGER = CustomLogger.init("KAFKA", KafkaOrderProducer.class);

    @Value("${mymarket.order.kafka.topic.name:order}")
    private String topic;

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public KafkaOrderProducer(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(OrderCreatedEvent orderCreatedEvent) {
        final String partitionKey = orderCreatedEvent.getOrder().getId().toString();
        final ListenableFuture<SendResult<String, OrderCreatedEvent>> future = kafkaTemplate.send(topic, partitionKey, orderCreatedEvent);

        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, OrderCreatedEvent> result) {
                LOGGER.debug(
                        "[KAFKA] Sent message=[{}] with offset = [{}]",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            }

            @Override
            public void onFailure(Throwable ex) {
                LOGGER.error("[KAFKA] Kafka produce failed : {}", ex.getCause());
            }
        });
    }
}
