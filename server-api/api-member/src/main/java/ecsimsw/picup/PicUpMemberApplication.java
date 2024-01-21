package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class PicUpMemberApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PicUpMemberApplication.class);
        app.run(args);
    }
}
