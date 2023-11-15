package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class PicUpAlbumApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication app = new SpringApplication(PicUpAlbumApplication.class);
//        if(app.getAdditionalProfiles().isEmpty()) {
//            app.setAdditionalProfiles("dev");
//        }
        app.run(args);
    }
}
