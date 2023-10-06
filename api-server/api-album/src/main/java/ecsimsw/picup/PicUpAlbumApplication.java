package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PicUpAlbumApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PicUpAlbumApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
}
