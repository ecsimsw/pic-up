package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class PicUpAlbumApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PicUpAlbumApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
}
