package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
public class PicUpMemberApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PicUpMemberApplication.class);
        if(app.getAdditionalProfiles().isEmpty()) {
            app.setAdditionalProfiles("dev");
        }
        app.run(args);
    }
}
