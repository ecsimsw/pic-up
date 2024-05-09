package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BatchApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
}

