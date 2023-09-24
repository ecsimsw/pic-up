package ecsimsw.mymarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class MyMarketMemberApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MyMarketMemberApplication.class);
        app.setAdditionalProfiles("dev");
        app.run(args);
    }
}
