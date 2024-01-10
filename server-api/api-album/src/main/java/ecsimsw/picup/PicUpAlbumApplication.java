package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableRetry
@SpringBootApplication
public class PicUpAlbumApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication app = new SpringApplication(PicUpAlbumApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
}
