package ecsimsw.picup;

import ecsimsw.auth.anotations.EnableSimpleAuth;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableSimpleAuth
@EnableRetry
@SpringBootApplication
public class PicUpAlbumApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication app = new SpringApplication(PicUpAlbumApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
}
