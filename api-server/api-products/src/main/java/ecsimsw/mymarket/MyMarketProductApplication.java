package ecsimsw.mymarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@ServletComponentScan
@EnableJpaAuditing
@EnableAsync
@SpringBootApplication
public class MyMarketProductApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MyMarketProductApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
}
