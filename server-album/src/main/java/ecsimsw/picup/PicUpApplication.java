package ecsimsw.picup;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@Log4j2
@EnableScheduling
@EnableRetry
@SpringBootApplication
public class PicUpApplication {

    public static void main(String[] args) {
        var app = new SpringApplication(PicUpApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
}
