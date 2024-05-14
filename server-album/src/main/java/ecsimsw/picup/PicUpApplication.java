package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PicUpApplication {

    public static void main(String[] args) {
        var app = new SpringApplication(PicUpApplication.class);
        app.setAdditionalProfiles("prod");
        app.run(args);
    }
}
