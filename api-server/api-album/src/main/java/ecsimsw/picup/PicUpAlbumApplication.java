package ecsimsw.picup;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@EnableRetry
@EnableAsync
@SpringBootApplication
public class PicUpAlbumApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication app = new SpringApplication(PicUpAlbumApplication.class);
        app.setAdditionalProfiles("dev");
        final ConfigurableApplicationContext run = app.run(args);
        final TestClass bean = run.getBean(TestClass.class);
        while (true) {
            Thread.sleep(1000);
            bean.testSend();
        }
    }
}

@Component
class TestClass {

    private final RabbitTemplate rabbitTemplate;

    public TestClass(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void testSend() {
        rabbitTemplate.convertAndSend(
            "global.exchange",
            "file.deletion",
            "hi"
        );
        System.out.println("sent");
    }
}
