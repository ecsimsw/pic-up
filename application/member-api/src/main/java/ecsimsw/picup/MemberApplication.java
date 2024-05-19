package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MemberApplication {

    public static void main(String[] args) {
        var app = new SpringApplication(MemberApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
}