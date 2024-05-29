package ecsimsw.picup.service;

import ecsimsw.picup.domain.MemberEventRepository;
import ecsimsw.picup.domain.MemberEvent_;
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
public class MemberEventPublishingScheduler {

    public static final String GLOBAL_EXCHANGE = "global_exchange";
    public static final String SIGN_UP_QUEUE_ROUTING_KEY = "sign_up";
    public static final String USER_DELETE_QUEUE_ROUTING_KEY = "user_delete";

    private final MemberEventRepository memberEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void publishCreationEvent() {
        var events = memberEventRepository.findAll(
            PageRequest.of(0, 10, Sort.by(Direction.ASC, MemberEvent_.CREATED_AT))
        );
        events.forEach( e -> {
            if(e.isSignUpEvent()) {
                rabbitTemplate.convertAndSend(GLOBAL_EXCHANGE, SIGN_UP_QUEUE_ROUTING_KEY,
                    new SignUpEventMessage(e.getUserId(), e.getLimitAsBytes())
                );
            }
            if(e.isDeletionEvent()) {
                rabbitTemplate.convertAndSend(GLOBAL_EXCHANGE, USER_DELETE_QUEUE_ROUTING_KEY, e
                    .getUserId()
                );
            }
        });
        memberEventRepository.deleteAll(events);
    }
}
