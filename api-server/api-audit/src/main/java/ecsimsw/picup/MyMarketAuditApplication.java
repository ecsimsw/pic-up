package ecsimsw.picup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@EnableMongoAuditing
@SpringBootApplication
public class MyMarketAuditApplication {

    public static void main(String[] args) {
        final SpringApplication app = new SpringApplication(MyMarketAuditApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
}
