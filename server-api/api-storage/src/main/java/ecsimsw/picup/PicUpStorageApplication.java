package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class PicUpStorageApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PicUpStorageApplication.class);
        app.setAdditionalProfiles("prod");
        app.run(args);
    }
}
