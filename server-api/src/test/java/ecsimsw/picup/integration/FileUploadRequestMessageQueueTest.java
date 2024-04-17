package ecsimsw.picup.integration;

import ecsimsw.picup.mq.ImageFileMessageQueue;
import ecsimsw.picup.mq.RabbitMQConfig;
import ecsimsw.picup.mq.MessageBrokerDownException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.retry.annotation.EnableRetry;

import static ecsimsw.picup.env.AlbumFixture.RESOURCES;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@EnableRetry
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = ImageFileMessageQueue.class)
class FileUploadRequestMessageQueueTest {

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

    @DisplayName("message queue 와의 연결에 실패하는 경우 지정된 횟수만큼 재시도한다.")
    @Test
    void retryInvalidStorageSeverDown() {
        doThrow(AmqpConnectException.class)
            .when(rabbitTemplate).convertAndSend(anyString(), any(Object.class));

        assertThatThrownBy(
            () -> imageFileMessageQueue.offerDeleteAllRequest(RESOURCES)
        ).isInstanceOf(MessageBrokerDownException.class);

        verify(rabbitTemplate, times(RabbitMQConfig.CONNECTION_RETRY_COUNT))
            .convertAndSend(queue.getName(), RESOURCES);
    }

    @DisplayName("재시도 도중 정상 응답 되는 경우를 예외없이 정상 응답한다.")
    @Test
    void validResponseWhileRetry() {
        doThrow(AmqpConnectException.class)
            .doNothing()
            .when(rabbitTemplate).convertAndSend(anyString(), any(Object.class));

        assertDoesNotThrow(() -> imageFileMessageQueue.offerDeleteAllRequest(RESOURCES));
        verify(rabbitTemplate, times(2))
            .convertAndSend(queue.getName(), RESOURCES);
    }
}
