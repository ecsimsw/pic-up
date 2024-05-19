package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AlbumApplication {

    public static void main(String[] args) {
        var app = new SpringApplication(AlbumApplication.class);
        app.setAdditionalProfiles("prod");
        app.run(args);
    }
}
