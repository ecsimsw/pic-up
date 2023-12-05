package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PicUpStorageApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PicUpStorageApplication.class);
        app.run(args);
    }
}
