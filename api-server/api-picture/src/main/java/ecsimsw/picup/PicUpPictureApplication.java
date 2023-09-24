package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PicUpPictureApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PicUpPictureApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
}
