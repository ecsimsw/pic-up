package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StorageApplication {

    public static void main(String[] args) {
        var app = new SpringApplication(StorageApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
}
