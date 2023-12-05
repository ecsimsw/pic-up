package ecsimsw.picup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PicUpStorageApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PicUpStorageApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
}
