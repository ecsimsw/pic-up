package ecsimsw.picup.service;

import ecsimsw.picup.domain.SignUpEventRepository;
import ecsimsw.picup.domain.SignUpEvent_;
import ecsimsw.picup.dto.SignUpEventMessage;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberCreationEventScheduler {

    public static final String GLOBAL_EXCHANGE = "global_exchange";
    public static final String SIGN_UP_QUEUE_ROUTING_KEY = "sign_up";

    private final SignUpEventRepository signUpEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void publishCreationEvent() {
        var events = signUpEventRepository.findAll(
            PageRequest.of(0, 10, Sort.by(Direction.ASC, SignUpEvent_.CREATED_AT))
        );
        for (var e : events) {
            rabbitTemplate.convertAndSend(
                GLOBAL_EXCHANGE,
                SIGN_UP_QUEUE_ROUTING_KEY,
                new SignUpEventMessage(e.getUserId(), e.getLimitAsBytes())
            );
        }
        signUpEventRepository.deleteAll(events);
    }
}
