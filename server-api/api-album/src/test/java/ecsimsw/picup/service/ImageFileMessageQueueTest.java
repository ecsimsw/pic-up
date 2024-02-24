package ecsimsw.picup.service;

import ecsimsw.picup.mq.ImageFileMessageQueue;
import ecsimsw.picup.mq.exception.MessageBrokerDownException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.TestPropertySource;

import static ecsimsw.picup.env.AlbumFixture.RESOURCES;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

// XXX :: Need to test with spring container, for using @Retryable
@TestPropertySource(locations = "/mq.properties")
@EnableRetry
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = ImageFileMessageQueue.class)
class ImageFileMessageQueueTest {

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private Queue queue;

    @Autowired
    private ImageFileMessageQueue imageFileMessageQueue;

    @BeforeEach
    void init() {
        when(queue.getName()).thenReturn("queueName");
    }

    @DisplayName("재시도 도중 정상 응답 되는 경우를 테스트한다.")
    @Test
    void validResponseWhileRetry() {
        doThrow(AmqpConnectException.class)
            .doNothing()
            .when(rabbitTemplate).convertAndSend(anyString(), any(Object.class));

        assertDoesNotThrow(
            () -> imageFileMessageQueue.offerDeleteAllRequest(RESOURCES)
        );

        verify(rabbitTemplate, times(2))
            .convertAndSend(queue.getName(), RESOURCES);
    }

    @DisplayName("message queue 와의 연결에 실패하는 경우 지정된 횟수만큼 재시도한다.")
    @Test
    void retryInvalidStorageSeverDown(
        @Value("${mq.server.connection.retry.cnt}") int retryCount
    ) {
        doThrow(AmqpConnectException.class)
            .when(rabbitTemplate).convertAndSend(anyString(), any(Object.class));

        assertThatThrownBy(
            () -> imageFileMessageQueue.offerDeleteAllRequest(RESOURCES)
        ).isInstanceOf(MessageBrokerDownException.class);

        verify(rabbitTemplate, times(retryCount))
            .convertAndSend(queue.getName(), RESOURCES);
    }

    @DisplayName("파일 삭제시 메시지 큐에 작업 요청, 비동기 처리한다.")
    @Test
    void delete() {
        assertDoesNotThrow(
            () -> imageFileMessageQueue.offerDeleteAllRequest(RESOURCES)
        );
    }
}
